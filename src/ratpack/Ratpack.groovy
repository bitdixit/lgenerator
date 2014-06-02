import static ratpack.groovy.Groovy.*
import ratpack.handling.Handler;
import ratpack.handling.Context;
import groovy.json.JsonSlurper

ratpack {
  handlers {
    get("api/v1/fonts")
    {
    	def json = new groovy.json.JsonBuilder()
    	json PDFCreator.getFonts()
		render json.toString()    	
    }
    post("api/v1/pdf")
    {
	  def type 
	  def texts
	  def rows
	  def cols
	  def font

      try
      {
		def slurper = new JsonSlurper()
		def result = slurper.parseText( request.body.text )

		type = result.type
		texts = result.texts
		rows = result.rows
		cols = result.cols

		if (result.containsKey("font"))
		{
			font = result.font
		} else
		{
			font = PDFCreator.getFonts()[0]
		}

		byte[] pdf = PDFCreator.generate(texts,cols,rows,font);

		if (type=="json")
		{
	      	response.contentType("application/json")
            def json = new groovy.json.JsonBuilder()
            json success: true, pdf: pdf.encodeBase64().toString()
			render json.toString()
		} else
		if (type=="pdf")
		{
            response.contentType("application/pdf")
            response.send pdf
		}
      } catch (e)
      {
        def json = new groovy.json.JsonBuilder()
        json success: false, error: e.message
      	response.status(500)
		render json.toString()
      }		
    }
    post("generate")
    {
      try
      {
          def form = context.parse(ratpack.form.Form);
          def content = form.get("content");
          def texts = []
          for (str in content.split("\\*"))
          {
              str = str.trim();
              if (str.length()>0) texts.add(str);
          } 
          int cols = form.get("cols").toInteger();
          int rows = form.get("rows").toInteger();
          def font = form.get("font")
          
          byte[] pdf = PDFCreator.generate(texts,cols,rows,font);
          response.contentType("application/pdf")
          response.send pdf

          return
      } catch (e)
      {
         render "Failed "+e.getMessage();         
      }
    }
    get {
      render groovyTemplate("index.html", fonts: PDFCreator.getFonts())
    }

  }
}


