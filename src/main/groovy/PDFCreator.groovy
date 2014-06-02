

class PDFCreator
{
    static def fontsDir = "/Users/adria/Developer/composer/fonts"

    static public ArrayList getFonts()
    {
        def allFiles = new File(fontsDir).listFiles();
        def fontFileNames = []
        for (file in allFiles)
        {
            if (file.name.endsWith(".ttf") || file.name.endsWith(".otf"))
            {
                fontFileNames.add(file.name);
            }
        }
        return fontFileNames;   
    }  


    static public byte[] generate(ArrayList texts, int cols, int rows, String font)
    {
         font=fontsDir+"/"+font;
    
         def document = new com.lowagie.text.Document()
         def baos = new java.io.ByteArrayOutputStream()
         def writer = com.lowagie.text.pdf.PdfWriter.getInstance(document,baos)
         document.open() 
         def boxMargin = 10f
         def pageMargin = 20f;
         def cardMargin = 5f;
         def colSize = (document.getPageSize().getWidth() - (cols-1)*boxMargin - 2*pageMargin) / cols;
         def rowSize = (document.getPageSize().getHeight() - (rows-1)*boxMargin - 2*pageMargin) / rows;
         def cb = writer.getDirectContent()
         
         def fonts = []
         def baseFont = com.lowagie.text.pdf.BaseFont.createFont(font, com.lowagie.text.pdf.BaseFont.WINANSI, com.lowagie.text.pdf.BaseFont.EMBEDDED);
         for (def fontSize = 240 ; fontSize > 6 ; fontSize-=2)
             fonts.add(new com.lowagie.text.Font(baseFont, fontSize));
         
         def textCount = 0;
         
         while (textCount < texts.size)
         {
             if (textCount>0) document.newPage(); 
             cb.setLineWidth(2.0f)  
             cb.setGrayStroke(0.95f)
             for (def row = 0 ; row < rows && textCount < texts.size ; ++row)
             {
                for (def col = 0; col < cols && textCount < texts.size ; ++col)
                {
                     float x1 = (pageMargin+col*colSize+col*boxMargin) as float
                     float y1 = document.getPageSize().getHeight()-(pageMargin+row*rowSize+row*boxMargin) as float
                     float x2 = (pageMargin+(col+1)*colSize+col*boxMargin) as float
                     float y2 = document.getPageSize().getHeight()-(pageMargin+(row+1)*rowSize+row*boxMargin) as float
                     
                     cb.moveTo(x1,y1) 
                     cb.lineTo(x1,y2)
                     cb.lineTo(x2,y2)
                     cb.lineTo(x2,y1)
                     cb.lineTo(x1,y1)
                     cb.stroke()                      
                     
                     def fontNo = 0;
                     def fontFits = false
                     while (fontFits == false && fontNo<fonts.size()-1) 
                     {
                         float inter = fonts[fontNo].getSize()*1.0f as int;
                       
                         def myText = new com.lowagie.text.Phrase(texts[textCount],fonts[fontNo]);
                         def ct = new com.lowagie.text.pdf.ColumnText(cb);
                         ct.setSimpleColumn(myText, (x1+cardMargin)  as float, (y1-cardMargin)  as float, (x2-cardMargin)  as float, (y2+cardMargin) as float, inter, com.lowagie.text.Element.ALIGN_CENTER);
                         def status = ct.go(true);
                         fontFits = !com.lowagie.text.pdf.ColumnText.hasMoreText(status);
                         if (!fontFits) fontNo++;
                     }                 
                     
                     def inter = fonts[fontNo].getSize()*1.0f as int;
                     def ct = new com.lowagie.text.pdf.ColumnText(cb);    
                     def myText = new com.lowagie.text.Phrase(texts[textCount],fonts[fontNo]);
                     ct.setSimpleColumn(myText, (x1+cardMargin)  as float, (y1-cardMargin)  as float, (x2-cardMargin)  as float, (y2+cardMargin) as float, inter, com.lowagie.text.Element.ALIGN_CENTER);
                     ct.go();
             
                     textCount++;
                }
             }
             }
                 
         document.close()
         return baos.toByteArray();
    }
}