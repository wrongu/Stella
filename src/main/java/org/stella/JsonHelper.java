package org.stella;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.*;

/**
 * A class with static helper methods for manipulating JSON. JsonHelper.applyDelta is probably the most useful.
 * 
 * What is delta-JSON? It's a (generally) small JSON object that expresses the difference between larger JSON objects.
 * It is meant to express the idea that if object B is only slightly different than object A, you can just write A + d = B, 
 * where d is the delta-JSON object. Naturally, this equation can be reversed: A = B - d. In situations like this, -d is
 * the _inverse_ of d. The inverse depends entirely on what A and B are.
 * 
 * An example of delta-JSON:
 * 
 * A = {
 * 	"name": "stella",
 *  "version": "0.0.1",
 *  "goes-up-to": 11
 * }
 * 
 * d = {
 *  "version": "0.0.2",
 *  "goes-up-to": null,
 *  "author": "wrongu"
 * }
 * 
 * B = A + d = {
 *  "name": "stella",
 *  "version": "0.0.2",
 *  "author": "wrongu"
 * }
 * 
 * inverse-d = {
 *  "version": "0.0.1",
 *  "goes-up-to": 11,
 *  "author": null
 * }
 * 
 * @author wrongu
 *
 */
public class JsonHelper {
	
	/**
	 * Recursively apply delta to the given root node.
	 * 
	 * @param base the root node of the (generally) large document
	 * @param delta the delta-json to alter base
	 * @return a DeltaResults object that has two JsonNode members: result (i.e. base+delta), and inverse (i.e. -delta) such that base==applyDelta(result, inverse)
	 */
	public static DeltaResults applyDelta(JsonNode base, JsonNode delta){
		DeltaResults dr = new DeltaResults();
		// if object: create or recurse
		// otherwise: swap out
		if(base.isObject() && delta.isObject()){
			// we know they are objects, so casting is safe
			ObjectNode baseObj = ((ObjectNode) base);
			ObjectNode deltaObj = ((ObjectNode) delta);
			// initialize results to new JsonObjects
			dr.result = JsonNodeFactory.instance.objectNode();
			dr.inverse = JsonNodeFactory.instance.objectNode();
			// loop over all child nodes in delta
			Iterator<Entry<String, JsonNode>> itr = deltaObj.getFields();
			while(itr.hasNext()){
				Entry<String, JsonNode> pair = itr.next();
				String key = pair.getKey();
				JsonNode dChild = pair.getValue();
				if(baseObj.has(pair.getKey())){
					JsonNode bChild = base.get(key);
					// so far delta matches base, so we recurse one level deeper
					if(dChild.isNull()){
						// one caveat: delta's 'null' value means remove it (i.e. just don't set it at all)
						// store previous value in inverse
						((ObjectNode)dr.inverse).put(key, bChild);
					} else if(bChild.equals(dChild)){
						// subtrees are equal. there is nothing to update.
						// so, we put the existing value back into the result and leave inverse blank
						((ObjectNode)dr.result).put(key, bChild);
					} else{
						// still more to process in the subtree. recursing.
						DeltaResults child_dr = applyDelta(bChild, dChild);
						((ObjectNode)dr.result).put(key, child_dr.result);
						((ObjectNode)dr.inverse).put(key, child_dr.inverse);
					}
				} else{
					// creating a new field
					((ObjectNode)dr.result).put(key, dChild);
					((ObjectNode)dr.inverse).put(key, JsonNodeFactory.instance.nullNode());
				}
			}
			// looping over child nodes of base (to set elements that weren't altered by delta)
			itr = baseObj.getFields();
			while(itr.hasNext()){
				Entry<String, JsonNode> pair = itr.next();
				String key = pair.getKey();
				JsonNode bChild = pair.getValue();
				if(!deltaObj.has(key)) ((ObjectNode)dr.result).put(key, bChild);
			}
		} else{
			// not an object, so we just set the new value
			// (and store the previous value in 'inverse' so that it can be undone)
			dr.inverse = base;
			dr.result = delta;
		}
		return dr;
	}
	
	public static class DeltaResults{
		public JsonNode result;
		public JsonNode inverse;
	}

	// TESTING
	private static int testCount = 1;
	
	private static void testDelta(JsonNode base, JsonNode delta){
		System.out.println("DELTA TEST "+(testCount++));
		System.out.println("--Base--\n"+base);
		System.out.println("--Delta--\n"+delta);
		DeltaResults dr = applyDelta(base, delta);
		System.out.println("--Result--\n"+dr.result);
		System.out.println("--Inverse--\n"+dr.inverse);
		dr = applyDelta(dr.result, dr.inverse);
		System.out.println("--Result^Inverse => result--\n"+dr.result);
		System.out.println("--Result^Inverse => inverse--\n"+dr.inverse);
		System.out.println("--Undo " + (base.equals(dr.result) ? "worked" : "failed") + "--\n");
	}
	
	public static void main(String[] args){
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory f = mapper.getJsonFactory();
		
		String jArr = "[0, 1, 2, 3]";
		String jFloat = "3.14159";
		String jBase = "{\"a\":[0, 1, 2, 3], \"b\":3.14159, \"c\": {\"d\": false, \"e\":4, \"f\": true}}";
		String jDelta = "{\"a\":[14], \"b\":null, \"c\": {\"d\": false, \"e\":7}}";
		
		try {
			JsonNode nodeArr = f.createJsonParser(jArr).readValueAsTree();
			JsonNode nodeFloat = f.createJsonParser(jFloat).readValueAsTree();
			JsonNode nodeBase = f.createJsonParser(jBase).readValueAsTree();
			JsonNode nodeDelta = f.createJsonParser(jDelta).readValueAsTree();
			
			testDelta(nodeArr, nodeFloat);
			testDelta(nodeBase, nodeDelta);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
