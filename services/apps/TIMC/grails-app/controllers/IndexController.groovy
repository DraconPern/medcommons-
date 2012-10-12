class IndexController {

    def index = { 

      render(text: "This is just a test page", 
             contentType:"text/plain")
    }
}
