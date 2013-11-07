Stella
======

NOTE: There is no source code yet. I will publish the source once the basic framework is done. This README is the 0th draft, and is subject to change. A lot. Please give feedback about the RESTful API draft below.

Stella is a Java implementation of the game-development tool server "Luna" created by Insomniac Games. You can read more about Luna [here][1]. To summarize, this is a JSON document editing server which accepts commands via a RESTful API. Document and asset editors may be implemented in any language as long as they communicate with Stella via GET and POST. The server runs locally on the same machine as the editors, although it could theoretically be accessed remotely. _Why_ go through all this trouble? This design decouples the editing tools from document management. The server _always_ maintains the authoritative copy of any document. It also maintains the undo/redo stack for each editor, opens and saves files, and will someday be able to communicate with a central repository over git. Multiple editors can access and edit the same document concurrently such that changes in one editor are immediately reflected in another.

The name "Stella" is meant to convey a connection to both Luna, and to [Project Trillek][2], the space-sim game for which this software is originally created.

Stella is open-source, and is covered by the GPL. Potential contributors are welcome to fork this repository and issue pull requests.

---------

RESTful API: first draft
-----

Here is an outline of the editor-server protocol. HTTP requests are sent to the local server (base-path `http://127.0.0.1:8080`).

Unless otherwise specified, all commands are with respect to a single document (that is, you can send a change to one document, no more no less). All requests' content is in JSON format. The content of all requests will minimally have the following content:

	{
	document: "path/to/file.json",
	editor: editor_id
	}

__Initialization__

* __Register Editor__ `POST @ /register`. I lied - this is the exception to using the minimum content. Each instance of each editor gets its own undo/redo stack, so the editor must register to get a unique id. The request contains the name of the editor:

		{ name: "SFXEditor" }

and the response will contain the id that the editor must keep track of and include with further requests:

		{ id: "server-generated-id" }

__Document Management__

* __Create a JSON file:__ `PUT @ /create`, using the minimum request with `document` and `editor`.
* open and save are not needed since documents are managed by a database (they are implicit)

__Editing__

* __Make a change:__ `POST @ /edit`, adding a field `data` which contains a list of edits (usually length 1). See the section on overwrites below. Basically, an edit has the form

		{
		property1: "newvalue1",
		property2: "newvalue2",
		property3: 
			{
				subproperty: "newsub"
			}
		}
    
* __Undo:__ `POST @ /undo`, using the minimum request.
* __Redo:__ `POST @ /redo`, ditto.

__Refresh__ Changes should not be stored in the editor. Any user actions should be immediately sent to the server. Meanwhile, the editor should also be polling the server frequently to get the latest authoritative copy.

* __Refresh:__ `GET @ /refresh`, 

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
