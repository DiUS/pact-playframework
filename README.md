pact play test
==============

play framework integrate test by [PACT](https://github.com/bethesque/pact) defined json file

It generate test case from a PACT defined json files for integration test.

Usage 
---

first, add a Rest Spec in play project and define the json files folder 

```
import com.dius.pact.play.PactPlaySpecification

class RestPactSpec extends PactPlaySpecification {

  def testJson = "test/resources/pacts"

}
```

and then add PACT defined json file in the folder
example json:

```
{
    "provider": {
        "name": "Activate Service"
    },
    "consumer": {
        "name": "Account App"
    },
    "interactions": [
        {
            "description": "Activate user with empty request body",
            "request": {
                "method": "post",
                "path": "/users/activate"
            },
            "response": {
                "status": 400,
                "headers": {
                    "Content-Type": "application/json; charset=UTF-8"
                }
        },
        {
            "description": "Activate user with validate request body",
            "request": {
                "method": "post",
                "path": "/users/activate",
                "headers": {
                    "Accept": "application/json"
                },
                "body": {
                    "activationToken": "activation-token"
                }
            },
            "response": {
                "status": 200,
                "headers": {
                    "Content-Type": "application/json; charset=UTF-8"
                }
            }
        }
    ]
}
```
