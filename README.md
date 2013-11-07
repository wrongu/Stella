Stella
======

NOTE: There is no source code yet. I will publish the source once the basic framework is done. This README is the 1st draft, and is subject to change. A lot. Please give feedback about the RESTful API draft below.

Stella is a Java implementation of the game-development tool server "Luna" created by Insomniac Games. You can read more about Luna [here][1]. To summarize, this is a JSON document editing server which accepts commands via a RESTful API. Document and asset editors may be implemented in any language as long as they communicate with Stella via GET and POST. The server runs locally on the same machine as the editors, although it could theoretically be accessed remotely. Why go through all this trouble? This design decouples the editing tools from document management. The server _always_ maintains the authoritative copy of any document. It also maintains the undo/redo stack for each editor, opens and saves files, and will someday be able to communicate with a central repository over git. Multiple editors can access and edit the same document concurrently such that changes in one editor are immediately reflected in another.

The name "Stella" is meant to convey a connection to both Luna, and to [Project Trillek][2], the space-sim game for which this software is originally created.

Stella is open-source, and is covered by the GPL. Potential contributors are welcome to fork this repository and issue pull requests.

---------

How it Works
------

The server keeps track JSON documents and the undo/redo stacks with a MongoDB database. Each JSON document corresponds to a file on the local disk. When Stella is launched for the first time, the user will be asked to choose a root-directory for all JSON documents. Requests to the API target a specific document using relative paths to this root directory.

If you want multiple editors to share an undo/redo stack, put them into the same _group_. The server will maintain a list of named groups. Edits are tagged in the database with a group id. Undo/Redo applies only to edits made by that editor's group. Editors are in charge of keeping track of their own group's id. You may get unexpected behavior if editors unknowingly share a group id. It's a feature, not a bug.

Editors
-----

Stella is a tool for game development, so let's look at a game example. Artists will create 3D models, textures, and sound-effects. There may be a programming api for ai behavior. JSON can be used as the glue that pulls them together; using _this_ model of a car and _that_ texture, it will make _vroom_ sounds when you press _SPACE_.

	{
	mesh: "assets/meshes/vehicles/car0.obj",
	texture: "assets/textures/brightred.jpg",
	sfx:  { file: "assets/sounds/vehicle/vroom.wav",
		trigger: "KEY-SPACE"} 
	}

This is what the editors are working with. Notice that we are __not__ talking about making the 3D model or the audio, but editing the JSON that associates them with each other. An Editor could, for example, be a GUI implemented in JavaScript that lets you change these values and renders the result on the screen.

Since the server always has the authoritative copy of any file, the editor must poll the server frequently for updates. The editor should never assume that its version of the JSON file is more recent than the server's.

RESTful API: first draft
------

Here is an outline of the editor-server protocol. HTTP requests are sent to the local server (base-path `http://127.0.0.1:8080`).

Unless otherwise specified, all commands are with respect to a single document (that is, you can send a change to one document, no more no fewer). 

__Headers__

The request header specifies metadata.

* `X-STELLA-GROUP` is the editor's group-id.
* `X-STELLA-DOCUMENT` is the relative path of the document to which the current request applies
* `X-STELLA-REVISION` is the revision of the current document last given 

__Content__

Content is in JSON format. More specifically, the server expects delta-JSON format. This means that the editor should only send the name-value pairs of properties that have changed. Think of it as the minimum-data that can be used by undo/redo (only update the fields that are affected). See the section "Editing Rules" below

__Document Management__

* __Create a JSON file:__ `PUT or POST /create`; requires the `X-STELLA-DOCUMENT` header
* __Delete a JSON file:__ `PUT or POST /delete`; requires the `X-STELLA-DOCUMENT` header
* open and save are not needed since documents are managed by a database (they are implicit)

__Editing__

* __Make a change:__ `POST /edit`; requires `GROUP` and `DOCUMENT` headers. Content is delta-JSON (see "Editing Rules" below).
* __Undo:__ `POST /undo`; requires `GROUP` and `DOCUMENT` headers, no content
* __Redo:__ `POST /redo`; requires `GROUP` and `DOCUMENT` headers, no content

__Refresh__ Changes should not be stored in the editor. Any user actions should be immediately sent to the server. Meanwhile, the editor should also be polling the server frequently to get the latest authoritative copy.

* __Refresh:__ `GET /refresh`; requiers all headers; The server's response-header will contain a _revision number_ in `X-STELLA-REVISION`. It is the responsibility of the editor to keep track of this number and supply it with the next refresh. This is used because the server sends back the delta-JSON of updates *since the last request*. If your editor doesn't support delta-JSON and you want the whole document each time, don't supply any revision number OR set it to 0.

__Git__

To be determined later. Things to think about now:

* git does not interface directly with mongodb documents, as far as I know. This means that there must be files on-disk. When does the server write to these?
* should the server maintain control over all git functions? (I'm going with "yes")
* the best solution I have so far is to replicate the github api inside stella.. or at least the few functions we need.

---

Editing Rules
----

Let's say we already have the following JSON document open:

	{
	name: "Laser Gun",
	mesh: "assets/mesh/weapons/blaster.obj",
	sfx: "asstes/sound/weapons/pewpew.wav",
	parent: "Player/Hand",
	color: "0xE15A30",
	}

(I'm just making stuff up here). We decide to change the name to "Laser," add a 'shader' field, and remove the color property. The editor should send a `POST` request to `/edit` with the following content:

	{
	document: "assets/weapons/lasergun.json",
	editor: "my-editor-id",
	delta:  {
		name: "Laser",
		shader: "shaders/fancyNewShader",
		color: null
		}
	}

To be pedantic, here is the resulting document:

	{
	name: "Laser",
	mesh: "assets/mesh/weapons/blaster.obj",
	sfx: "asstes/sound/weapons/pewpew.wav",
	parent: "Player/Hand",
	shader: "shaders/fancyNewShader"
	}

Notice that Stella will overwrite existing values and create any that don't already exist. Names are case sensitive. To erase a name-value pair, set the value to null.

Things are a little more subtle when changing values in nested objects and arrays. The rules are that arrays are swapped out wholesale (and therefore are somewhat inefficient and should be avoided); and, nested values are updated as far down the tree as possible. Consider this document:

	{
	name: "a deep object",
	theobject: {
		   subname: "the inner object",
		   level: 9000
		   }
	}

If I want to update level to 9001 and leave everything else unchanged, the request to `/edit` will look like this:

	{
	document: "deepexample.json",
	editor: "my-editor-id",
	delta:  { theobject: { level: 9001 } }
	}

Being pedantic again, here is the result (note that `subname` was unchanged):

	{
	name: "a deep object",
	theobject: {
		   subname: "the inner object",
		   level: 9001
		   }
	}
	
[1]: http://www.itshouldjustworktm.com/?p=875
[2]: http://www.trillek.org
