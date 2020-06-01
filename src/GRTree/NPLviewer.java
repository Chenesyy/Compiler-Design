package grtree ;

import java.awt.* ;
import java.awt.event.* ;
import java.io.* ;

/**
*  <html>
*  An application run from the command line. (See main method.)
*  Whenever user pushes button, last rule tree generated by NPL
*  is shown in a TreeScrollFrame.
*  <P>
*  <img src = "viewruletree.gif">
*  </html>
*/
public class NPLviewer extends Frame 
                       implements ActionListener {
 
   String where ;  // Where is tree expression?
 
   public NPLviewer(String w) {
      super("NPL Viewer") ; 
      where = w ;
      Button view = new Button("View Rule Tree") ; 
      view.addActionListener(this) ;
      this.addWindowListener( new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
           System.exit(0) ;
        }
      }) ;
      this.setLayout(new GridLayout(1,1,10,10)) ;
      this.add(view) ; 
      this.setSize(170,60) ; 
      this.setLocation(500,400) ;
      this.show() ;
   }

   public void actionPerformed(ActionEvent a) {
      try { 
         BufferedReader br = 
            new BufferedReader(new FileReader(where)) ; 
         TreeScrollFrame tsf =
            new TreeScrollFrame(TreeExpression.toTree(br.readLine())) ; 
         tsf.setLocation(600,40) ;
      }
      catch(Exception e) {
         System.out.println(e) ;
      }
   }

   /**
   *  <pre>
   *  NPLviewer is launched like this :
   *    c:\...> java grtree.NPLviewer  fn
   *  where fn is the correspondence 
   *  filename of the file containing the 
   *  tree expression that should be drawn each
   *  time the view button is pushed.
   *  </pre>
   */
   public static void main(String[] args) {
      NPLviewer nplViewer = new NPLviewer(args[0]) ;
   }

}
