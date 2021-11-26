# Java connector for ThingsDB

## Installation

For Maven users:

<dependency>
   <groupId>io.github.thingsdb</groupId>
   <artifactId>thingsdb</artifactId>
   <version>0.1.0</version>
</dependency>


## Query

Example

```java
import io.github.thingsdb.connector.Connector;
import io.github.thingsdb.connector.Result;
import io.github.thingsdb.connector.Vars;

...

Connector client = new Connector("localhost");  // or "hostname" and port
client
    .setDefaultScope("//stuff")     // change the default scope
    .connect()                      // connect to ThingsDB
    .authenticate("admin", "pass")  // token.. or username, password
    .get();                         // wait for the future to complete


// Perform some useless query
Vars vars = new Vars();
vars.setInt("a", 6);
vars.setInt("b", 7);
Result res = client.query("a * b;", vars).get();

assertEquals(res.unpackInt(), 42);
```
