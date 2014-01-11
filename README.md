Stella
======

Stella is two things: a protocol for an asset-managing server for editing JSON documents, and a Java implementation of that protocol. This is a tool primarily for game developers to make the process of writing "editors" easier. Stella was originally developed for [Project Trillek][3], but it is intended to be generic for any game developer.

Stella is inspired by [Luna Server][1] created by Insomniac Games. To summarize, it is a JSON document editing server which accepts commands via a [RESTful API][2]. Editors may be implemented in any language as long as they communicate with Stella via GET and POST (see the section on Editors, below). The server runs locally on the same machine as the editors, although it could theoretically be accessed remotely.

Why go through all this trouble? _This design decouples the editing tools from editing operations._ In other words, if your game development team wants a tool for editing lights in a scene, they can focus on the graphics and data, and Stella will take care of the open/close operations, undo/redo stacks, and (eventually) collaboration for you! This also makes your editors robust against crashes. All of your work is saved in the server, so the client can crash without losing data, or even losing the undo-history!

---------

Contributing
-------

Stella is open-source, and is covered by the GPL. Potential contributors are welcome to fork this repository and issue pull requests. Please include JavaDoc comments for all classes, public variables, and methods.

How it Works
------

The server _always_ maintains the authoritative copy of any document. It also maintains the undo/redo stack for each group of editors, opens and saves files, and will someday be able to communicate with a central repository over git. The server keeps track of JSON documents and the undo/redo stacks with a MongoDB database. Each JSON document corresponds to a file on the local disk. When Stella is launched for the first time, the user will be asked to choose a root-directory for all JSON documents. Requests to the API target a specific document using relative paths to this root directory.

Editors
-----

Stella is a tool for game development, so let's look at a game example. Artists will create 3D models, textures, and sound-effects. There may be a programming api for AI behavior. JSON can be used as the glue that pulls them together; using _this_ model of a car and _that_ texture, it will make _vroom_ sounds when you press _SPACE_.

	{
	mesh: "assets/meshes/vehicles/car0.obj",
	texture: "assets/textures/brightred.jpg",
	sfx:  {
		file: "assets/sounds/vehicle/vroom.wav",
		trigger: "KEY-SPACE"} 
	}

This is what the editors are working with. Notice that we are __not__ talking about making the 3D model or the audio, but editing the JSON that associates them with each other. An Editor could, for example, be a GUI implemented in JavaScript that lets you change these values and renders the result on the screen.

Since the server always has the authoritative copy of any file, the editor must poll the server frequently for updates. The editor should never assume that its version of the JSON file is more recent than the server's.

__Groups__

If you want multiple editors to share an undo/redo stack, put them into the same _group_. Edits are tagged in the database with a group id. Undo/Redo applies only to edits made by that editor's group. Editors are in charge of keeping track of their own group's id. You may get unexpected behavior if editors unknowingly share a group id. It's a feature, not a bug.

---

RESTful API: early draft (subject to change)
------

Here is an outline of the editor-server protocol. HTTP requests are sent to the local server (base-path `http://127.0.0.1:8080`).

Unless otherwise specified, all commands are with respect to a single document (that is, you can send a change to one document, no more no fewer).

__HTTP Headers__

Stella uses the following custom HTTP Headers:

* `X-STELLA-GROUP`, a string which defines the editor's group
* `X-STELLA-REVISION`, an unsigned integer specifying the last revision the editor received an update about
* `X-STELLA-ACTION`, a string which defines the editing action ("edit", "undo", or "redo")

__HTTP Content__

Content is in JSON format. More specifically, the server expects __delta-JSON__ format (a term invented for [Luna Server][1]). This means that the editor should only send the name-value pairs of properties that have _changed_. Think of it as the minimum-data that can be used by undo/redo (only update the fields that are affected). See the section "Editing Rules" below. Note that sending the whole document each time will work, but with more overhead.

__Document Management__

* __Create a JSON file:__ `PUT /api/{path}` where `{path}` is the relative path to the document you wish to create. The headers and body of the http request are ignored.
* __Delete a JSON file:__ `DELETE /api/{path}` where `{path}` is the relative path to the document you wish to delete. The headers and body of the http request are ignored.

__Editing__

All edits are communicated by a `POST` request to the document's URL. For each of these requests, editors are expected to supply their own `group` in the `X-STELLA-GROUP` header, or it will default to `""` (for a description of groups, see the section on "Editors" at the beginning of this document).

* __Make a change:__ `POST /api/{path}` where the `X-STELLA-ACTION` header is "edit", and the body is the delta-JSON of the change.
* __Undo:__ `POST /api/{path}` where the `X-STELLA-ACTION` header is "undo" and the body is ignored
* __Redo:__ `POST /api/{path}` where the `X-STELLA-ACTION` header is "redo" and the body is ignored

__Refresh__ Changes should not be stored in the editor. Any user actions should be immediately sent to the server. Meanwhile, the editor should also be polling the server frequently to get the latest authoritative copy.

* __Refresh:__ `GET /api/{path}` where metadata is supplied in the HTTP headers for both request and response. `X-STELLA-REVISION` should be set (or will default to 0). The response body will contain delta-JSON for all changes to the requested document _since the given revision_. If the revision is set to 0, the response body will contain the full document.

---

Editing Behavior: an example
----

Let's say we already have open the document "entities/weapons/lasergun.json":

	{
	name: "Laser Gun",
	mesh: "assets/mesh/weapons/blaster.obj",
	sfx: "asstes/sound/weapons/pewpew.wav",
	parent: "Player/Hand",
	color: "0xE15A30",
	}

(I'm just making stuff up here). We decide to change the name to "Laser," add a 'shader' field, and remove the color property. The editor should send a `POST` request to `/api/entities/weapons/lasergun.json` with the HTTP header `X-STELLA-ACTION` as "edit" and the following HTTP body:

	{
	name: "Laser",
	shader: "shaders/fancyNewShader",
	color: null
	}

To be pedantic, here is the resulting document:

	{
	name: "Laser",
	mesh: "assets/mesh/weapons/blaster.obj",
	sfx: "asstes/sound/weapons/pewpew.wav",
	parent: "Player/Hand",
	shader: "shaders/fancyNewShader"
	}

Notice that __Stella will overwrite existing values and create any that don't already exist__. Names are case sensitive, of course. __To erase a name-value pair, set the value to null__.

Things are a little more subtle when changing values in nested objects and arrays. The rules are that arrays are swapped out wholesale (and therefore are somewhat inefficient and should be avoided); and, nested values are updated as far down the tree as possible. Consider this document:

	{
	name: "a deep object",
	theobject: {
		   subname: "the inner object",
		   level: 9000
		   }
	}

If I want to update level to 9001 and leave everything else unchanged, the request body (delta-JSON) will look like this:

	{
	theobject: { level: 9001 } }
	}

Being pedantic again, here is the result (note that `subname` was unchanged):

	{
	name: "a deep object",
	theobject: {
		   subname: "the inner object",
		   level: 9001
		   }
	}

----

Planned Future Features
-----

__Rename__ documents

__Git__ for synchronizing documents between a team. Things to think about now:

* git does not interface directly with mongodb documents, as far as I know. This means that there must be files on-disk. When does the server write to these?
* should the server maintain control over all git functions? (I'm going with "yes")
* the best solution I have so far is to replicate the github api inside stella.. or at least the few functions we need.

__Schema__ for verifying content format of edits. Likely to use [JsonSchema][4].

__Remote Servers__ as an alternative to Git. This would effectively make live collaboration on JSON documents viable. Security is the biggest concern.

---

	
[1]: http://www.itshouldjustworktm.com/?p=875
[2]: http://www.restapitutorial.com
[3]: http://www.trillek.org
[4]: http://json-schema.org/
