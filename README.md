Stella
======

NOTE: There is no source code yet. I will publish the source once the basic framework is done. This README is the 0th draft, and is subject to change. A lot. Please give feedback about the RESTful API draft below.

Stella is a Java implementation of the game-development tool server "Luna" created by Insomniac Games. You can read more about Luna [here][1]. To summarize, this is a JSON document editing server which accepts commands via a RESTful API. Document and asset editors may be implemented in any language as long as they communicate with Stella via GET and POST. The server runs locally on the same machine as the editors, although it could theoretically be accessed remotely. __Why__ go through all this trouble? This design decouples the editing tools from document management. Stella maintains the undo/redo stack for each editor, opens and saves files, and will someday be able to communicate with a central repository over git. Multiple editors can access and edit the same document concurrently such that changes in one editor are immediately reflected in another.

The name "Stella" is meant to convey a connection to both Luna, and to [Project Trillek][2], the space-sim game for which this software is originally created.

Stella is open-source, and is covered by the GPL. Potential contributors are welcome to fork this repository and issue pull requests.

[1](http://www.itshouldjustworktm.com/?p=875)
[2](http://www.trillek.org)

---------

RESTful API: first draft
-----

Here is an outline of the editor-server protocol. HTTP requests are sent to the local server (base-path `http://0.0.0.0:8080`).

Unless otherwise specified, all commands are with respect to a single document (that is, you can send a change to one document, no more no less). All requests' content is in JSON format. The content of a request will minimally have the following content:

  {
    document: "path/to/file.json"
  }

__Document Management__

* __Create a JSON file:__ `GET` at `/create`, using the above minimum-json request
* open and save are not needed since documents are managed by a database (they are implicit)

__Editing__

* __Make a change:__ `POST` at `/edit`, adding a field `data` which contains a list of edits (usually length 1). See the section on overwrites below. Basically, an edit has the form

    {
      property1: "newvalue1",
      property2: "newvalue2",
      property3: 
      {
        subproperty: "newsub"
      }
    }
    
* __Undo:__ `POST` at `/undo`, using the minimum request.
* __Redo:__ `POST` at `/redo`, ditto.

__Git__

This is a serverely un-thought-out API. In any case, git integration is low priority.

The big question is whether a single editor can commit changes made by other editors, or if the server should maintain exclusive control. Also, it is not easy to handle conflicts via editors. Anything not covered by the RESTful protocol can be done manually from the command line or the git editor of choice. Here is one potential API:

* __Commit:__ `GET` at `/git-commit`, using the following structure for the content:

    {
      documents: ["path/to/document1", "path/to/document2", ...]
      message: "this is the commit message"
    }
    
* __Pull:__ `GET` at `/git-pull`, this command will fetch and merge the latest info from the central repository.
*__Push:__ `GET` at `/git-push`, send all local commits to the central repository (as a pull-request)
* __Branch and Checkout:__ not sure if these should be included or not.
