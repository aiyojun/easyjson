# easyjson

Make JSON parsing&amp;building easy.

## dependency

Only one, Jackson.

## Usage

See unit test directory.

```java
import com.jpro.easyjson.Json;  // The only one class need to import!

public class EasyJson {
    @Test
    public void parseJsonString() {
        /* The basic usage! Sometimes, you may want to extract field from a json string. */
        Json json = Json.parse("{\"id\":\"jpro\",\"data\":[{\"a\":1,\"b\":\"easyjson\"}]}");
        System.out.println(json.get("id").value());
        System.out.println(json.get("data").dumps());
        System.out.println(json.dumps());
    }

    @Test
    public void parseObjectMap() {
        /* It maybe useful in springboot,
           like http 'request' in every rest-controller! */
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("id", "jpro");
            put("data", new ArrayList<String>() {{
                add("a"); add("b"); add("c");
            }});
        }};
        Json json = Json.parse(map);
        System.out.println(json.get("id").value());
        System.out.println(json.get("data").dumps());
        System.out.println(json.dumps());
    }

    @Test
    public void buildEasyJsonByYourSelf() {
        /* You can build more complex json by yourself! It's up to you!
         * Use case: return a http response in springboot.
         */
        Json json = new Json("data", Json.array(new ArrayList<String>() {{ add("a"); add("b"); add("c"); }}));
        System.out.println(json.dumps());
    }
}
```