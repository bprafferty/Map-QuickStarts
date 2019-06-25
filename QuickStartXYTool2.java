// This program gives the ability to easily add a point theme from
// a file of points with lines in the form longitude,latitude,name,etc.
// in world coordinates.  We also give the user a choice of creating
// a new shape file.  Letting the user browse to a folder would prevent
// a LOT of user errors.  This should really be done......maybe later.
// A csv file like MughalEmperors.txt is more complicated than data.csv
// in my in class example,and the lat and long are also wanted in the
// attribute table.  So this code does that. See the createDbfFields() method.

// see QuickStartXY23Delhi.java for tips on write difficulties
//TIPS: two sources of shapefile writing errors were:
  // 1. too short a field width for a string in createDbfFields()
  // 2. column name with all digits and special characters in
           // createDbfFields
  // 3.  field width limit on VARCHAR is 250, more or less

import javax.swing.*;
import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.awt.*;
import java.awt.event.*;
import com.esri.mo2.ui.bean.*; // beans used: Map,Layer,Toc,TocAdapter,Tool
        // TocEvent,Legend(a legend is part of a toc),ActateLayer
import com.esri.mo2.ui.tb.ZoomPanToolBar;
import com.esri.mo2.ui.tb.SelectionToolBar;
import com.esri.mo2.ui.ren.LayerProperties;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import com.esri.mo2.data.feat.*; //ShapefileFolder, ShapefileWriter
import com.esri.mo2.map.dpy.FeatureLayer;
import com.esri.mo2.map.dpy.BaseFeatureLayer;
import com.esri.mo2.map.draw.SimpleMarkerSymbol;
import com.esri.mo2.map.draw.BaseSimpleRenderer;
import com.esri.mo2.file.shp.*;
import com.esri.mo2.map.dpy.Layerset;
import com.esri.mo2.ui.bean.Tool;
import java.awt.geom.*;
import com.esri.mo2.cs.geom.*; //using Envelope, Point, BasePointsArray

public class QuickStartXYTool2 extends JFrame {
  static Map map = new Map();
  static String [] attrib = {"col1","col2","col3","col4","col5","col6","col7","cl8","col9","col10",
     "col11","col12","col3","col14","col15","col16","col17","col18","col19"};
  static String [] lengths = {"","","","","","","","","","","","","","","","","","","",""};
  static String [] precs = {"","","","","","","","","","","","","","","","","","","",""};
  static boolean fullMap = true;  // Map not zoomed
  Legend legend;
  Legend legend2;
  Layer layer = new Layer();
  Layer layer2 = new Layer();
  Layer layer3 = null;
  static AcetateLayer acetLayer;
  static com.esri.mo2.map.dpy.Layer layer4;
  com.esri.mo2.map.dpy.Layer activeLayer;
  int activeLayerIndex;
  com.esri.mo2.cs.geom.Point initPoint,endPoint;
  double distance;
  JMenuBar mbar = new JMenuBar();
  JMenu file = new JMenu("File");
  JMenu theme = new JMenu("Theme");
  JMenu layercontrol = new JMenu("LayerControl");
  JMenuItem attribitem = new JMenuItem("open attribute table",
                            new ImageIcon("tableview.gif"));
  JMenuItem createlayeritem  = new JMenuItem("create layer from selection",
                    new ImageIcon("Icon0915b.jpg"));
  static JMenuItem promoteitem = new JMenuItem("promote selected layer",
                    new ImageIcon("promote.jpg"));
  JMenuItem demoteitem = new JMenuItem("demote selected layer",
                    new ImageIcon("demote.jpg"));
  JMenuItem printitem = new JMenuItem("print",new ImageIcon("print.gif"));
  JMenuItem addlyritem = new JMenuItem("add layer",new ImageIcon("addtheme.gif"));
  JMenuItem remlyritem = new JMenuItem("remove layer",new ImageIcon("delete.gif"));
  JMenuItem propsitem = new JMenuItem("Legend Editor",new ImageIcon("properties.gif"));
  Toc toc = new Toc();
  String s1 = "C:\\ESRI\\MOJ20\\Samples\\Data\\USA\\States.shp";
  String s2 = "C:\\ESRI\\MOJ20\\Samples\\Data\\USA\\capitals.shp";
  String datapathname = "";
  String legendname = "";
  ZoomPanToolBar zptb = new ZoomPanToolBar();
  static SelectionToolBar stb = new SelectionToolBar();
  JToolBar jtb = new JToolBar();
  ComponentListener complistener;
  JLabel statusLabel = new JLabel("status bar    LOC");
  static JLabel milesLabel = new JLabel("   DIST:  0 mi    ");
  static JLabel kmLabel = new JLabel("  0 km    ");
  java.text.DecimalFormat df = new java.text.DecimalFormat("0.000");
  JPanel myjp = new JPanel();
  JPanel myjp2 = new JPanel();
  JButton prtjb = new JButton(new ImageIcon("print.gif"));
  JButton addlyrjb = new JButton(new ImageIcon("addtheme.gif"));
  JButton ptrjb = new JButton(new ImageIcon("pointer.gif"));
  JButton distjb = new JButton(new ImageIcon("measure_1.gif"));
  JButton XYjb = new JButton("XY");
  //Arrow arrow = new Arrow();
  //DistanceTool distanceTool= new DistanceTool();
  ActionListener lis;
  ActionListener layerlis;
  ActionListener layercontrollis;
  TocAdapter mytocadapter;
  static Envelope env;
  public QuickStartXYTool2() {

    super("Quick Start");
    //distanceTool.setMeasureUnit(com.esri.mo2.util.Units.MILES);
    //map.setMapUnit(com.esri.mo2.util.Units.MILES);
    this.setBounds(50,50,700,450);
    zptb.setMap(map);
    stb.setMap(map);
    setJMenuBar(mbar);
    ActionListener lisZoom = new ActionListener() {
	  public void actionPerformed(ActionEvent ae){
	    fullMap = false;}}; // can change a boolean here
	ActionListener lisFullExt = new ActionListener() {
	  public void actionPerformed(ActionEvent ae){
	    fullMap = true;}};
	// next line gets ahold of a reference to the zoomin button
	JButton zoomInButton = (JButton)zptb.getActionComponent("ZoomIn");
	JButton zoomFullExtentButton =
	        (JButton)zptb.getActionComponent("ZoomToFullExtent");
	JButton zoomToSelectedLayerButton =
	      (JButton)zptb.getActionComponent("ZoomToSelectedLayer");
	zoomInButton.addActionListener(lisZoom);
	zoomFullExtentButton.addActionListener(lisFullExt);
	zoomToSelectedLayerButton.addActionListener(lisZoom);
	complistener = new ComponentAdapter () {
	  public void componentResized(ComponentEvent ce) {
	    if(fullMap) {
	      map.setExtent(env);
	      map.zoom(1.0);    //scale is scale factor in pixels
	      map.redraw();
	    }
	  }
	};
    addComponentListener(complistener);
    lis = new ActionListener() {public void actionPerformed(ActionEvent ae){
	  Object source = ae.getSource();
	  if (source == prtjb || source instanceof JMenuItem ) {
        com.esri.mo2.ui.bean.Print mapPrint = new com.esri.mo2.ui.bean.Print();
        mapPrint.setMap(map);
        mapPrint.doPrint();// prints the map
        }
      else if (source == ptrjb) {
		Arrow arrow = new Arrow();
		map.setSelectedTool(arrow);
	    }
	  else if (source == distjb) {
		DistanceTool distanceTool = new DistanceTool();
		map.setSelectedTool(distanceTool);
        }
	  else if (source == XYjb) {
		try {
		  AddXYtheme addXYtheme = new AddXYtheme();
		  addXYtheme.setMap(map);
		  addXYtheme.setVisible(false);// the file chooser needs a parent
		    // but the parent can stay behind the scenes
		  map.redraw();
		  } catch (IOException e){}
	    }
	  else
	    {
		try {
	      AddLyrDialog aldlg = new AddLyrDialog();
	      aldlg.setMap(map);
	      aldlg.setVisible(true);
	    } catch(IOException e){}
      }
    }};
    layercontrollis = new ActionListener() {public void
                actionPerformed(ActionEvent ae){
	  String source = ae.getActionCommand();

	  if (source == "promote selected layer")
		map.getLayerset().moveLayer(activeLayerIndex,++activeLayerIndex);
      else
        map.getLayerset().moveLayer(activeLayerIndex,--activeLayerIndex);
      enableDisableButtons();
      map.redraw();
    }};
    layerlis = new ActionListener() {public void actionPerformed(ActionEvent ae){
	  Object source = ae.getSource();
	  if (source instanceof JMenuItem) {
		String arg = ae.getActionCommand();
		if(arg == "add layer") {
          try {
	        AddLyrDialog aldlg = new AddLyrDialog();
	        aldlg.setMap(map);
	        aldlg.setVisible(true);
          } catch(IOException e){}
	      }
	    else if(arg == "remove layer") {
	      try {
			com.esri.mo2.map.dpy.Layer dpylayer =
			   legend.getLayer();
			map.getLayerset().removeLayer(dpylayer);
			map.redraw();
			remlyritem.setEnabled(false);
			propsitem.setEnabled(false);
			attribitem.setEnabled(false);
			promoteitem.setEnabled(false);
			demoteitem.setEnabled(false);
			stb.setSelectedLayer(null);
			zptb.setSelectedLayer(null);
	      } catch(Exception e) {}
	      }
	    else if(arg == "Legend Editor") {
          LayerProperties lp = new LayerProperties();
          lp.setLegend(legend);
          lp.setSelectedTabIndex(0);
          lp.setVisible(true);
	    }
	    else if (arg == "open attribute table") {
	      try {
	        layer4 = legend.getLayer();
            AttrTab attrtab = new AttrTab();
            attrtab.setVisible(true);
	      } catch(IOException ioe){}
	    }
        else if (arg=="create layer from selection") {
	      com.esri.mo2.map.draw.BaseSimpleRenderer sbr = new
	        com.esri.mo2.map.draw.BaseSimpleRenderer();
		  com.esri.mo2.map.draw.SimpleFillSymbol sfs = new
		    com.esri.mo2.map.draw.SimpleFillSymbol();// for polygons
		  sfs.setSymbolColor(new Color(255,255,0)); // mellow yellow
		  sfs.setType(com.esri.mo2.map.draw.SimpleFillSymbol.FILLTYPE_SOLID);
		  sfs.setBoundary(true);
	      layer4 = legend.getLayer();
	      FeatureLayer flayer2 = (FeatureLayer)layer4;
	      // select, e.g., Montana and then click the
	      // create layer menuitem; next line verifies a selection was made

	      //next line creates the 'set' of selections
	      if (flayer2.hasSelection()) {
		    SelectionSet selectset = flayer2.getSelectionSet();
	        // next line makes a new feature layer of the selections
	        FeatureLayer selectedlayer = flayer2.createSelectionLayer(selectset);
	        sbr.setLayer(selectedlayer);
	        sbr.setSymbol(sfs);
	        selectedlayer.setRenderer(sbr);
	        Layerset layerset = map.getLayerset();
	        // next line places a new visible layer, e.g. Montana, on the map
	        layerset.addLayer(selectedlayer);
	        //selectedlayer.setVisible(true);
	        if(stb.getSelectedLayers() != null)
	          promoteitem.setEnabled(true);
	        try {
	          legend2 = toc.findLegend(selectedlayer);
		    } catch (Exception e) {}

		    CreateShapeDialog csd = new CreateShapeDialog(selectedlayer);
		    csd.setVisible(true);
	        Flash flash = new Flash(legend2);
	        flash.start();
	        map.redraw(); // necessary to see color immediately

		  }
	    }
      }
    }};
    toc.setMap(map);
    mytocadapter = new TocAdapter() {
	  public void click(TocEvent e) {
		System.out.println(activeLayerIndex+ "dex");
	    legend = e.getLegend();
	    activeLayer = legend.getLayer();
	    stb.setSelectedLayer(activeLayer);
	    zptb.setSelectedLayer(activeLayer);
	    // get acive layer index for promote and demote
	    activeLayerIndex = map.getLayerset().indexOf(activeLayer);
	    // layer indices are in order added, not toc order.

	    remlyritem.setEnabled(true);
	    propsitem.setEnabled(true);
	    attribitem.setEnabled(true);
	    enableDisableButtons();
   	  }
    };
    map.addMouseMotionListener(new MouseMotionAdapter() {
	  public void mouseMoved(MouseEvent me) {
		com.esri.mo2.cs.geom.Point worldPoint = null;
		if (map.getLayerCount() > 0) {
		  worldPoint = map.transformPixelToWorld(me.getX(),me.getY());
		  String s = "X:"+df.format(worldPoint.getX())+" "+
		             "Y:"+df.format(worldPoint.getY());
		  statusLabel.setText(s);
	      }
	    else
	      statusLabel.setText("X:0.000 Y:0.000");
      }
    });

    toc.addTocListener(mytocadapter);
    remlyritem.setEnabled(false); // assume no layer initially selected
    propsitem.setEnabled(false);
    attribitem.setEnabled(false);
    promoteitem.setEnabled(false);
    demoteitem.setEnabled(false);
    printitem.addActionListener(lis);
    addlyritem.addActionListener(layerlis);
    remlyritem.addActionListener(layerlis);
    propsitem.addActionListener(layerlis);
    attribitem.addActionListener(layerlis);
    createlayeritem.addActionListener(layerlis);
    promoteitem.addActionListener(layercontrollis);
    demoteitem.addActionListener(layercontrollis);
    file.add(addlyritem);
    file.add(printitem);
    file.add(remlyritem);
    file.add(propsitem);
    theme.add(attribitem);
    theme.add(createlayeritem);
    layercontrol.add(promoteitem);
    layercontrol.add(demoteitem);
    mbar.add(file);
    mbar.add(theme);
    mbar.add(layercontrol);
    prtjb.addActionListener(lis);
    prtjb.setToolTipText("print map");
    addlyrjb.addActionListener(lis);
    addlyrjb.setToolTipText("add layer");
    ptrjb.addActionListener(lis);
    distjb.addActionListener(lis);
    XYjb.addActionListener(lis);
    XYjb.setToolTipText("add a layer of points from a file");
    prtjb.setToolTipText("pointer");
    distjb.setToolTipText("press-drag-release to measure a distance");
    jtb.add(prtjb);
    jtb.add(addlyrjb);
    jtb.add(ptrjb);
    jtb.add(distjb);
    jtb.add(XYjb);
    myjp.add(jtb);
    myjp.add(zptb); myjp.add(stb);
    myjp2.add(statusLabel);
    myjp2.add(milesLabel);myjp2.add(kmLabel);
    getContentPane().add(map, BorderLayout.CENTER);
    getContentPane().add(myjp,BorderLayout.NORTH);
    getContentPane().add(myjp2,BorderLayout.SOUTH);
    addShapefileToMap(layer,s1);
    addShapefileToMap(layer2,s2);
    getContentPane().add(toc, BorderLayout.WEST);
  }
  private void addShapefileToMap(Layer layer,String s) {
    String datapath = s; //"C:\\ESRI\\MOJ10\\Samples\\Data\\USA\\States.shp";
    layer.setDataset("0;"+datapath);
    map.add(layer);
  }
  public static void main(String[] args) {
    QuickStartXYTool2 qstart = new QuickStartXYTool2();
    qstart.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            System.out.println("Thanks, Quick Start exits");
            System.exit(0);
        }
    });
    qstart.setVisible(true);
    env = map.getExtent();
  }
  private void enableDisableButtons() {
    int layerCount = map.getLayerset().getSize();
    if (layerCount < 2) {
      promoteitem.setEnabled(false);
      demoteitem.setEnabled(false);
      }
    else if (activeLayerIndex == 0) {
      demoteitem.setEnabled(false);
      promoteitem.setEnabled(true);
	  }
    else if (activeLayerIndex == layerCount - 1) {
      promoteitem.setEnabled(false);
      demoteitem.setEnabled(true);
	  }
	else {
	  promoteitem.setEnabled(true);
	  demoteitem.setEnabled(true);
    }
  }
}
// following is an Add Layer dialog window
class AddLyrDialog extends JDialog {
  Map map;
  ActionListener lis;
  JButton ok = new JButton("OK");
  JButton cancel = new JButton("Cancel");
  JPanel panel1 = new JPanel();
  com.esri.mo2.ui.bean.CustomDatasetEditor cus = new com.esri.mo2.ui.bean.
    CustomDatasetEditor();
  AddLyrDialog() throws IOException {
	setBounds(50,50,520,430);
	setTitle("Select a theme/layer");
	addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	    setVisible(false);
	  }
    });
	lis = new ActionListener() {
	  public void actionPerformed(ActionEvent ae) {
	    Object source = ae.getSource();
	    if (source == cancel)
	      setVisible(false);
	    else {
	      try {
			setVisible(false);
			map.getLayerset().addLayer(cus.getLayer());
			map.redraw();
			if (QuickStartXYTool2.stb.getSelectedLayers() != null)
			  QuickStartXYTool2.promoteitem.setEnabled(true);
		  } catch(IOException e){}
	    }
	  }
    };
    ok.addActionListener(lis);
    cancel.addActionListener(lis);
    getContentPane().add(cus,BorderLayout.CENTER);
    panel1.add(ok);
    panel1.add(cancel);
    getContentPane().add(panel1,BorderLayout.SOUTH);
  }
  public void setMap(com.esri.mo2.ui.bean.Map map1){
	map = map1;
  }
}
class AddXYtheme extends JDialog {
  Map map;
  // make arraylist of arraylists
  ArrayList<ArrayList> forest = new ArrayList<ArrayList>();
  int fieldCount = 0;
  Vector xCoord = new Vector();
  Vector yCoord = new Vector();
  JFileChooser jfc = new JFileChooser();
  BasePointsArray bpa = new BasePointsArray();
  FeatureLayer XYlayer;
  AddXYtheme() throws IOException {
	for (int i=0;i<100;i++)
	  forest.add(new ArrayList());

	setBounds(50,50,520,430);
	//File dirInit = new File("..");
	jfc.showOpenDialog(this);
	//jfc.setCurrentDirectory(dirInit);
	try {
	  File file  = jfc.getSelectedFile();
	  FileReader fred = new FileReader(file);
	  BufferedReader in = new BufferedReader(fred);
	  String s; // = in.readLine();
	  double x,y;
	  int n = 0;
	  int m = 0;
	  String str;
	  while ((s = in.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(s,",");
		x = Double.parseDouble(st.nextToken());
		xCoord.addElement(x+"");
		y = Double.parseDouble(st.nextToken());
		yCoord.addElement(y+"");
		bpa.insertPoint(n++,new com.esri.mo2.cs.geom.Point(x,y));
		while (st.hasMoreTokens()) {

	      String sss = st.nextToken();

		  forest.get(m++).add(sss);
          fieldCount=m;
        }
        m=0;
	  }

	} catch (IOException e){}

	XYfeatureLayer xyfl = new XYfeatureLayer(bpa,map,forest,fieldCount,xCoord,yCoord);
	XYlayer = xyfl;
	xyfl.setVisible(true);
	map = QuickStartXYTool2.map;
	map.getLayerset().addLayer(xyfl);
	map.redraw();
	CreateXYShapeDialog xydialog =
			      new CreateXYShapeDialog(XYlayer);
	xydialog.setVisible(true);
  }
  public void setMap(com.esri.mo2.ui.bean.Map map1){
  	map = map1;
  }
}
class XYfeatureLayer extends BaseFeatureLayer {
  BaseFields fields;
  String [] attrib = QuickStartXYTool2.attrib;
  String [] lengths = QuickStartXYTool2.lengths;
  String [] precs = QuickStartXYTool2.precs;
  int fieldCount = 0;
  private java.util.Vector featureVector;
  public XYfeatureLayer(BasePointsArray bpa,Map map,ArrayList<ArrayList> forest,int fieldcount,Vector xCoord,Vector yCoord) {
	fieldCount = fieldcount;
	createFeaturesAndFields(bpa,map,forest,fieldcount,xCoord,yCoord);
	BaseFeatureClass bfc = getFeatureClass("MyPoints",bpa);
	setFeatureClass(bfc);
	BaseSimpleRenderer srd = new BaseSimpleRenderer();
	SimpleMarkerSymbol sms= new SimpleMarkerSymbol();
	sms.setType(SimpleMarkerSymbol.CIRCLE_MARKER);
	sms.setSymbolColor(new Color(255,0,0));
	sms.setWidth(5);
	srd.setSymbol(sms);
	setRenderer(srd);
	// without setting layer capabilities, the points will not
	// display (but the toc entry will still appear)
	XYLayerCapabilities lc = new XYLayerCapabilities();
	setCapabilities(lc);
  }
  private void createFeaturesAndFields(BasePointsArray bpa,Map map,ArrayList<ArrayList> forest,int fieldcount,Vector xCoord,Vector yCoord) {
	featureVector = new java.util.Vector();
	fields = new BaseFields();
	createDbfFields();

	for(int i=0;i<bpa.size();i++) {
	  BaseFeature feature = new BaseFeature();  //feature is a row
	  feature.setFields(fields);
	  com.esri.mo2.cs.geom.Point p = new
	    com.esri.mo2.cs.geom.Point(bpa.getPoint(i));
	  feature.setValue(0,p);
	  feature.setValue(1,new Integer(0));  // point data
	  for (int j=0;j<fieldCount;j++){
		if (precs[j].equals("")){

	      feature.setValue(j+4,(String)forest.get(j).get(i));}
	    else {
	      float f = Float.valueOf((String)forest.get(j).get(i));
	      feature.setValue(j+4,new Float(f));
	    }
	  }
	  //feature.setValue(4,"rice");
	  //float f =  Float.valueOf("4.2");
	  //feature.setValue(5,new Float(f));
	  feature.setValue(2,(String)xCoord.elementAt(i));
	  feature.setValue(3,(String)yCoord.elementAt(i));
	  feature.setDataID(new BaseDataID("MyPoints",i));
	  featureVector.addElement(feature);
	}
  }
  private void createDbfFields() {
	fields.addField(new BaseField("#SHAPE#",Field.ESRI_SHAPE,0,0));
	fields.addField(new BaseField("ID",java.sql.Types.INTEGER,9,0));
	fields.addField(new BaseField("Longitude",java.sql.Types.VARCHAR,12,0));
	fields.addField(new BaseField("Latitude",java.sql.Types.VARCHAR,12,0));
	// from hereon, the user picks attribute names;
	// attrib is an array of strings gathered from the user
	// the second BaseField parameter is the width of the field
	// column names are limited to 10 characters
	CreateAttribDialog cat = new CreateAttribDialog();
	cat.setVisible(true);
    // in the loop below we make the fields either of type char or type numeric
    //  For numeric the field is generic type DECIMAL and the value must be an Object,
    // so the string captured from the csv file is converted to a wrapped float
	for (int j=0;j<fieldCount;j++)
      if (precs[j].equals(""))  {
		if (lengths[j].equals("")){
		  //System.out.println("no params");
	      fields.addField(new BaseField(attrib[j],java.sql.Types.VARCHAR,35,0));}
	    else{
			//System.out.println("one params");
	      int intleng = Integer.parseInt(lengths[j]);
	      fields.addField(new BaseField(attrib[j],java.sql.Types.VARCHAR,intleng,0));}
	    }

	  else {//System.out.println("two params");
		int intlen = Integer.parseInt(lengths[j]);
		int intprec = Integer.parseInt(precs[j]);
		// Java generic database type DECIMAL works well for numbers like 12.456
	    fields.addField(new BaseField(attrib[j],java.sql.Types.DECIMAL,intlen,intprec));
      }

  }
  public BaseFeatureClass getFeatureClass(String name,BasePointsArray bpa){
    com.esri.mo2.map.mem.MemoryFeatureClass featClass = null;
    try {
	  featClass = new com.esri.mo2.map.mem.MemoryFeatureClass(MapDataset.POINT,
	    fields);
    } catch (IllegalArgumentException iae) {}
    featClass.setName(name);
    for (int i=0;i<bpa.size();i++) {
	  featClass.addFeature((Feature) featureVector.elementAt(i));
    }
    return featClass;
  }
  private final class XYLayerCapabilities extends
       com.esri.mo2.map.dpy.LayerCapabilities {
    XYLayerCapabilities() {
	  for (int i=0;i<this.size(); i++) {
		setAvailable(this.getCapabilityName(i),true);
		setEnablingAllowed(this.getCapabilityName(i),true);
		getCapability(i).setEnabled(true);
	  }
    }
  }
}
class AttrTab extends JDialog {
  JPanel panel1 = new JPanel();
  com.esri.mo2.map.dpy.Layer layer = QuickStartXYTool2.layer4;
  JTable jtable = new JTable(new MyTableModel());
  JScrollPane scroll = new JScrollPane(jtable);

  public AttrTab() throws IOException {
  	setBounds(70,70,450,350);
  	setTitle("Attribute Table");
  	addWindowListener(new WindowAdapter() {
  	  public void windowClosing(WindowEvent e) {
  	    setVisible(false);
  	  }
    });
    scroll.setHorizontalScrollBarPolicy(
	   JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	// next line necessary for horiz scrollbar to work
	jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	TableColumn tc = null;
	int numCols = jtable.getColumnCount();
	//jtable.setPreferredScrollableViewportSize(
		//new java.awt.Dimension(440,340));
	for (int j=0;j<numCols;j++) {
	  tc = jtable.getColumnModel().getColumn(j);
	  tc.setMinWidth(50);
    }
    getContentPane().add(scroll,BorderLayout.CENTER);
  }
}
class MyTableModel extends AbstractTableModel {
 // the required methods to implement are getRowCount,
 // getColumnCount, getValueAt
  com.esri.mo2.map.dpy.Layer layer = QuickStartXYTool2.layer4;
  MyTableModel() {
	qfilter.setSubFields(fields);
	com.esri.mo2.data.feat.Cursor cursor = flayer.search(qfilter);
	while (cursor.hasMore()) {
		ArrayList inner = new ArrayList();
		Feature f = (com.esri.mo2.data.feat.Feature)cursor.next();
		inner.add(0,String.valueOf(row));
		for (int j=1;j<fields.getNumFields();j++) {
		  inner.add(f.getValue(j).toString());
		}
	    data.add(inner);
	    row++;
    }
  }
  FeatureLayer flayer = (FeatureLayer) layer;
  FeatureClass fclass = flayer.getFeatureClass();
  String columnNames [] = fclass.getFields().getNames();
  ArrayList data = new ArrayList();
  int row = 0;
  int col = 0;
  BaseQueryFilter qfilter = new BaseQueryFilter();
  Fields fields = fclass.getFields();
  public int getColumnCount() {
	return fclass.getFields().getNumFields();
  }
  public int getRowCount() {
	return data.size();
  }
  public String getColumnName(int colIndx) {
	return columnNames[colIndx];
  }
  public Object getValueAt(int row, int col) {
	  ArrayList temp = new ArrayList();
	  temp =(ArrayList) data.get(row);
      return temp.get(col);
  }
}
class CreateShapeDialog extends JDialog {
  String name = "";
  String path = "";
  JButton ok = new JButton("OK");
  JButton cancel = new JButton("Cancel");
  JTextField nameField = new JTextField("enter layer name here, then hit ENTER",25);
  com.esri.mo2.map.dpy.FeatureLayer selectedlayer;
  ActionListener lis = new ActionListener() {public void actionPerformed(ActionEvent ae) {
	Object o = ae.getSource();
	if (o == nameField) {
	  name = nameField.getText().trim();
	  //path = ((ShapefileFolder)(QuickStartXYTool2.layer4.getLayerSource())).getPath();
	  System.out.println(path+"    " + name);
    }
	else if (o == cancel)
      setVisible(false);
	else {
	  try {
		ShapefileWriter.writeFeatureLayer(selectedlayer,"C:\\esri\\moj20\\shapefile","MughalEmperors",0);
	  } catch(Exception e) {System.out.println("write error");}
	  setVisible(false);
    }
  }};

  JPanel panel1 = new JPanel();
  JLabel centerlabel = new JLabel();
  //centerlabel;
  CreateShapeDialog (com.esri.mo2.map.dpy.FeatureLayer layer5) {
	selectedlayer = layer5;
    setBounds(40,350,450,150);
    setTitle("Create new shapefile?");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	    setVisible(false);
	  }
    });
    nameField.addActionListener(lis);
    ok.addActionListener(lis);
    cancel.addActionListener(lis);
    String s = "<HTML> To make a new shapefile from the new layer, enter<BR>" +
      "the new name you want for the layer and click OK.<BR>" +
      "You can then add it to the map in the usual way.<BR>"+
      "Click ENTER after replacing the text with your layer name";
    centerlabel.setHorizontalAlignment(JLabel.CENTER);
    centerlabel.setText(s);
    getContentPane().add(centerlabel,BorderLayout.CENTER);
    panel1.add(nameField);
    panel1.add(ok);
    panel1.add(cancel);
    getContentPane().add(panel1,BorderLayout.SOUTH);
  }
}
class CreateXYShapeDialog extends JDialog {
  String name = "";
  String path = "";
  JButton ok = new JButton("OK");
  JButton cancel = new JButton("Cancel");
  JTextField nameField = new JTextField("enter layer name here, then hit ENTER",35);
  JTextField pathField = new JTextField("enter full path name here, then hit ENTER",35);
  com.esri.mo2.map.dpy.FeatureLayer XYlayer;
  ActionListener lis = new ActionListener() {public void actionPerformed(ActionEvent ae) {
	Object o = ae.getSource();
	if (o == pathField) {
	  path = pathField.getText().trim();
	  System.out.println(path);
    }
    else if (o == nameField) {
	  name = nameField.getText().trim();
	  //path = ((ShapefileFolder)(QuickStartXYTool2.layer4.getLayerSource())).getPath();
	  System.out.println(path+"    " + name);
    }
	else if (o == cancel)
      setVisible(false);
	else {  // ok button clicked
	  try {System.out.println("writing shapefile");
		ShapefileWriter.writeFeatureLayer(XYlayer,path,name,0);
		// the following hard-coded line works with Mughal_Emperors.txt
		//ShapefileWriter.writeFeatureLayer(XYlayer,"C:\\MughalBuild\\Shapefile","MughalBattle",0);
		//ShapefileWriter.writeFeatureLayer(XYlayer,"C:\\temp2","DelhiSultanate",0);
	  } catch(Exception e) {System.out.println("write error");}
	  setVisible(false);
    }
  }};

  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();
  JLabel centerlabel = new JLabel();
  //centerlabel;
  CreateXYShapeDialog (com.esri.mo2.map.dpy.FeatureLayer layer5) {
	XYlayer = layer5;
    setBounds(40,250,600,300);
    setTitle("Create new shapefile?");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	    setVisible(false);
	  }
    });
    nameField.addActionListener(lis);
    pathField.addActionListener(lis);
    ok.addActionListener(lis);
    cancel.addActionListener(lis);
    String s = "<HTML> To make a new shapefile from the new layer, enter<BR>"+
      "the new name you want for the layer and hit ENTER.<BR>" +
      "then enter a path to the folder you want to use<BR>"+
       "hit ENTER once again. As an example type C:\\mylayers<BR>"+
      "You can then add it to the map in the usual way<BR>"+
      "Click ENTER after replacing the text with your layer name";
    centerlabel.setHorizontalAlignment(JLabel.CENTER);
    centerlabel.setText(s);
    //getContentPane().add(centerlabel,BorderLayout.CENTER);
    panel1.add(centerlabel);
    panel1.add(nameField);
    panel1.add(pathField);
    panel2.add(ok);
    panel2.add(cancel);
    getContentPane().add(panel2,BorderLayout.SOUTH);
    getContentPane().add(panel1,BorderLayout.CENTER);
  }
}
class CreateAttribDialog extends JDialog {
  JLabel help = new JLabel("<HTML>Please enter column names for your attribute data."+
    "Your longitude and latitude are done automatically, so just enter other <BR>" +
    "names. For example if your csv row is  -117,32,San Diego,California "+
    "then you might choose to enter City in the first textfield, and State<BR>"+
    "in the second. AFTER EACH TEXTFIELD ENTRY, HIT ENTER. NOTE: if you want your field to be numeric, then fill in <BR>"+
    "the two short fields.  E.g.filling in 12 and 5 in those two short fields will allow 12 digits in the number, with <BR>"+
    "5 of those digits after a decimal point, e.g. 1234567.12345");
  JButton Ok = new JButton("Ok");
  JLabel label1 = new JLabel("Attrib 1");
  JTextField attrib1 = new JTextField("",12);
  JTextField len1 = new JTextField("",2);
  JTextField prec1 = new JTextField("",1);
  JLabel label2 = new JLabel("Attrib 2");
  JTextField attrib2 = new JTextField("",12);
  JTextField len2 = new JTextField("",2);
  JTextField prec2 = new JTextField("",1);
  JLabel label3 = new JLabel("Attrib 3");
  JTextField attrib3 = new JTextField("",12);
  JTextField len3 = new JTextField("",2);
  JTextField prec3 = new JTextField("",1);
  JLabel label4 = new JLabel("Attrib 4");
  JTextField attrib4 = new JTextField("",12);
  JTextField len4 = new JTextField("",2);
  JTextField prec4 = new JTextField("",1);
  JLabel label5 = new JLabel("Attrib 5");
  JTextField attrib5 = new JTextField("",12);
  JTextField len5 = new JTextField("",2);
  JTextField prec5 = new JTextField("",1);
  JLabel label6 = new JLabel("Attrib 6");
  JTextField attrib6 = new JTextField("",12);
  JTextField len6 = new JTextField("",2);
  JTextField prec6 = new JTextField("",1);
  JLabel label7 = new JLabel("Attrib 7");
  JTextField attrib7 = new JTextField("",12);
  JTextField len7 = new JTextField("",2);
  JTextField prec7 = new JTextField("",1);
  JLabel label8 = new JLabel("Attrib 8");
  JTextField attrib8 = new JTextField("",12);
  JTextField len8 = new JTextField("",2);
  JTextField prec8 = new JTextField("",1);
  JLabel label9 = new JLabel("Attrib 9");
  JTextField attrib9 = new JTextField("",12);
  JTextField len9 = new JTextField("",2);
  JTextField prec9 = new JTextField("",1);
  JLabel label10 = new JLabel("Attrib 10");
  JTextField attrib10 = new JTextField("",12);
  JTextField len10 = new JTextField("",2);
  JTextField prec10 = new JTextField("",1);
  JLabel label11 = new JLabel("Attrib 11");
  JTextField attrib11 = new JTextField("",12);
  JTextField len11 = new JTextField("",2);
  JTextField prec11 = new JTextField("",1);
  JLabel label12 = new JLabel("Attrib 12");
  JTextField attrib12 = new JTextField("",12);
  JTextField len12 = new JTextField("",2);
  JTextField prec12 = new JTextField("",1);
  JLabel label13 = new JLabel("Attrib 12");
  JTextField attrib13 = new JTextField("",12);
  JTextField len13 = new JTextField("",2);
  JTextField prec13 = new JTextField("",1);
  JLabel label14 = new JLabel("Attrib 14");
  JTextField attrib14 = new JTextField("",12);
  JTextField len14 = new JTextField("",2);
  JTextField prec14 = new JTextField("",1);
  JLabel label15 = new JLabel("Attrib 15");
  JTextField attrib15 = new JTextField("",12);
  JTextField len15 = new JTextField("",2);
  JTextField prec15 = new JTextField("",1);
  JLabel label16 = new JLabel("Attrib 16");
  JTextField attrib16 = new JTextField("",12);
  JTextField len16 = new JTextField("",2);
  JTextField prec16 = new JTextField("",1);
  JLabel label17 = new JLabel("Attrib 17");
  JTextField attrib17 = new JTextField("",12);
  JTextField len17 = new JTextField("",2);
  JTextField prec17 = new JTextField("",1);
  JLabel label18 = new JLabel("Attrib 18");
  JTextField attrib18 = new JTextField("",12);
  JTextField len18 = new JTextField("",2);
  JTextField prec18 = new JTextField("",1);
  JLabel label19 = new JLabel("Attrib 19");
  JTextField attrib19 = new JTextField("",12);
  JTextField len19 = new JTextField("",2);
  JTextField prec19 = new JTextField("",1);
  JLabel label20 = new JLabel("Attrib 20");
  JTextField attrib20 = new JTextField("",12);
  JTextField len20 = new JTextField("",2);
  JTextField prec20 = new JTextField("",1);
  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();

  CreateAttribDialog(){
	//String [] attrib = QuickStartXYTool2.attrib;
	setModal(true);
    setBounds (250,550,770,350);
    setTitle("Enter Column Names");
    ActionListener lis = new ActionListener() {public void actionPerformed(ActionEvent ae) {
	  	Object o = ae.getSource();
		if (o == attrib1) {

			QuickStartXYTool2.attrib[0] = attrib1.getText().trim();}
	    else if (o == attrib2)
			QuickStartXYTool2.attrib[1] = attrib2.getText().trim();
		else if (o == attrib3)
			QuickStartXYTool2.attrib[2] = attrib3.getText().trim();
		else if (o == attrib4)
			QuickStartXYTool2.attrib[3] = attrib4.getText().trim();
		else if (o == attrib5)
	  	    QuickStartXYTool2.attrib[4] = attrib5.getText().trim();
	  	else if (o == attrib6)
	  	    QuickStartXYTool2.attrib[5] = attrib6.getText().trim();
	  	else if (o == attrib7)
	  	    QuickStartXYTool2.attrib[6] = attrib7.getText().trim();
	  	else if (o == attrib8)
	  	    QuickStartXYTool2.attrib[7] = attrib8.getText().trim();
	  	else if (o == attrib9)
	  	    QuickStartXYTool2.attrib[8] = attrib9.getText().trim();
	  	else if (o == attrib10)
	  	    QuickStartXYTool2.attrib[9] = attrib10.getText().trim();
	  	else if (o == attrib11)
	  	    QuickStartXYTool2.attrib[10] = attrib11.getText().trim();
	  	else if (o == attrib12)
	  	    QuickStartXYTool2.attrib[11] = attrib12.getText().trim();
	  	else if (o == attrib13)
	  	    QuickStartXYTool2.attrib[12] = attrib13.getText().trim();
	  	else if (o == attrib14)
	  	    QuickStartXYTool2.attrib[13] = attrib14.getText().trim();
	  	else if (o == attrib15)
	  	    QuickStartXYTool2.attrib[14] = attrib15.getText().trim();
	  	else if (o == attrib16)
	  	    QuickStartXYTool2.attrib[15] = attrib16.getText().trim();
	  	else if (o == attrib17)
	  	    QuickStartXYTool2.attrib[16] = attrib17.getText().trim();
	  	else if (o == attrib18)
	  	    QuickStartXYTool2.attrib[17] = attrib18.getText().trim();
	  	else if (o == attrib19)
	  	    QuickStartXYTool2.attrib[18] = attrib19.getText().trim();
	    else if (o == attrib20)
	  	    QuickStartXYTool2.attrib[19] = attrib20.getText().trim();

	  	else if (o == len1)
	  	    QuickStartXYTool2.lengths[0] = len1.getText().trim();
	  	else if (o == len2)
	  	    QuickStartXYTool2.lengths[1] = len2.getText().trim();
	  	else if (o == len3)
	  	    QuickStartXYTool2.lengths[2] = len3.getText().trim();
	  	else if (o == len4)
	  	    QuickStartXYTool2.lengths[3] = len4.getText().trim();
	  	else if (o == len5)
	  	    QuickStartXYTool2.lengths[4] = len5.getText().trim();
	  	else if (o == len6)
	  	    QuickStartXYTool2.lengths[5] = len6.getText().trim();
	  	else if (o == len7)
	  	    QuickStartXYTool2.lengths[6] = len7.getText().trim();
	  	else if (o == len8)
	  	    QuickStartXYTool2.lengths[7] = len8.getText().trim();
	  	else if (o == len9)
	  	    QuickStartXYTool2.lengths[8] = len9.getText().trim();
	  	else if (o == len10)
	  	    QuickStartXYTool2.lengths[9] = len10.getText().trim();
	  	else if (o == len11)
	  	    QuickStartXYTool2.lengths[10] = len11.getText().trim();
	  	else if (o == len12)
	  	    QuickStartXYTool2.lengths[11] = len12.getText().trim();
	    else if (o == len13)
	  	    QuickStartXYTool2.lengths[12] = len13.getText().trim();
	  	else if (o == len14)
	  	    QuickStartXYTool2.lengths[13] = len14.getText().trim();
	  	else if (o == len15)
	  	    QuickStartXYTool2.lengths[14] = len15.getText().trim();
	  	else if (o == len16)
	  	    QuickStartXYTool2.lengths[15] = len16.getText().trim();
	  	else if (o == len17)
	  	    QuickStartXYTool2.lengths[16] = len17.getText().trim();
	  	else if (o == len18)
	  	    QuickStartXYTool2.lengths[17] = len18.getText().trim();
	    else if (o == len19)
	  	    QuickStartXYTool2.lengths[18] = len19.getText().trim();
	  	else if (o == len20)
	  	    QuickStartXYTool2.lengths[19] = len20.getText().trim();

        else if (o == prec1)
	  	    QuickStartXYTool2.precs[0] = prec1.getText().trim();
	  	else if (o == prec2)
	  	    QuickStartXYTool2.precs[1] = prec2.getText().trim();
	  	else if (o == prec3)
	  	    QuickStartXYTool2.precs[2] = prec3.getText().trim();
	  	else if (o == prec4)
	  	    QuickStartXYTool2.precs[3] = prec4.getText().trim();
	    else if (o == prec5)
	  	    QuickStartXYTool2.precs[4] = prec5.getText().trim();
	  	else if (o == prec6)
	  	    QuickStartXYTool2.precs[5] = prec6.getText().trim();
	  	else if (o == prec7)
	  	    QuickStartXYTool2.precs[6] = prec7.getText().trim();
	  	else if (o == prec8)
	  	    QuickStartXYTool2.precs[7] = prec8.getText().trim();
	  	else if (o == prec9)
	  	    QuickStartXYTool2.precs[8] = prec9.getText().trim();
	    else if (o == prec10)
	  	    QuickStartXYTool2.precs[9] = prec10.getText().trim();
	  	else if (o == prec11)
	  	    QuickStartXYTool2.precs[10] = prec11.getText().trim();
	  	else if (o == prec12)
	  	    QuickStartXYTool2.precs[11] = prec12.getText().trim();
	  	else if (o == prec13)
	  	    QuickStartXYTool2.precs[12] = prec13.getText().trim();
	  	else if (o == prec14)
	  	    QuickStartXYTool2.precs[13] = prec14.getText().trim();
	  	else if (o == prec15)
	  	    QuickStartXYTool2.precs[14] = prec15.getText().trim();
	  	else if (o == prec16)
	  	    QuickStartXYTool2.precs[15] = prec16.getText().trim();
	  	else if (o == prec17)
	  	    QuickStartXYTool2.precs[16] = prec17.getText().trim();
	  	else if (o == prec18)
	  	    QuickStartXYTool2.precs[17] = prec18.getText().trim();
	  	else if (o == prec19)
	  	    QuickStartXYTool2.precs[18] = prec19.getText().trim();
	    else if (o == prec20)
	  	    QuickStartXYTool2.precs[19] = prec20.getText().trim();
	else if (o == Ok)
	  	    setVisible(false);

    }};

    Ok.addActionListener(lis);
    attrib1.addActionListener(lis);
    attrib2.addActionListener(lis);
    attrib3.addActionListener(lis);
    attrib4.addActionListener(lis);
    attrib5.addActionListener(lis);
    attrib6.addActionListener(lis);
    attrib7.addActionListener(lis);
    attrib8.addActionListener(lis);
    attrib9.addActionListener(lis);
    attrib10.addActionListener(lis);
    attrib11.addActionListener(lis);
    attrib12.addActionListener(lis);
    attrib13.addActionListener(lis);
    attrib14.addActionListener(lis);
    attrib15.addActionListener(lis);
    attrib16.addActionListener(lis);
    attrib17.addActionListener(lis);
    attrib18.addActionListener(lis);
    attrib19.addActionListener(lis);
    attrib20.addActionListener(lis);
    len1.addActionListener(lis);
	len2.addActionListener(lis);
	len3.addActionListener(lis);
	len4.addActionListener(lis);
	len5.addActionListener(lis);
	len6.addActionListener(lis);
	len7.addActionListener(lis);
	len8.addActionListener(lis);
	len9.addActionListener(lis);
	len10.addActionListener(lis);
	len11.addActionListener(lis);
	len12.addActionListener(lis);
	len13.addActionListener(lis);
	len14.addActionListener(lis);
	len15.addActionListener(lis);
	len16.addActionListener(lis);
	len17.addActionListener(lis);
	len18.addActionListener(lis);
	len19.addActionListener(lis);
    len20.addActionListener(lis);
    prec1.addActionListener(lis);
	prec2.addActionListener(lis);
	prec3.addActionListener(lis);
	prec4.addActionListener(lis);
	prec5.addActionListener(lis);
	prec6.addActionListener(lis);
	prec7.addActionListener(lis);
	prec8.addActionListener(lis);
	prec9.addActionListener(lis);
	prec10.addActionListener(lis);
	prec11.addActionListener(lis);
	prec12.addActionListener(lis);
	prec13.addActionListener(lis);
	prec14.addActionListener(lis);
	prec15.addActionListener(lis);
	prec16.addActionListener(lis);
	prec17.addActionListener(lis);
	prec18.addActionListener(lis);
	prec19.addActionListener(lis);
    prec20.addActionListener(lis);

    panel1.add(help);
	panel1.add(label1);panel1.add(attrib1);panel1.add(len1);panel1.add(prec1);
	panel1.add(label2);panel1.add(attrib2);panel1.add(len2);panel1.add(prec2);
	panel1.add(label3);panel1.add(attrib3);panel1.add(len3);panel1.add(prec3);
	panel1.add(label4);panel1.add(attrib4);panel1.add(len4);panel1.add(prec4);
	panel1.add(label5);panel1.add(attrib5);panel1.add(len5);panel1.add(prec5);
	panel1.add(label6);panel1.add(attrib6);panel1.add(len6);panel1.add(prec6);
	panel1.add(label7);panel1.add(attrib7);panel1.add(len7);panel1.add(prec7);
	panel1.add(label8);panel1.add(attrib8);panel1.add(len8);panel1.add(prec8);
	panel1.add(label9);panel1.add(attrib9);panel1.add(len9);panel1.add(prec9);
	panel1.add(label10);panel1.add(attrib10);panel1.add(len10);panel1.add(prec10);
	panel1.add(label11);panel1.add(attrib11);panel1.add(len11);panel1.add(prec11);
	panel1.add(label12);panel1.add(attrib12);panel1.add(len12);panel1.add(prec12);
	panel1.add(label13);panel1.add(attrib13);panel1.add(len13);panel1.add(prec13);
	panel1.add(label14);panel1.add(attrib14);panel1.add(len14);panel1.add(prec14);
	panel1.add(label15);panel1.add(attrib15);panel1.add(len15);panel1.add(prec15);
	panel1.add(label16);panel1.add(attrib16);panel1.add(len16);panel1.add(prec16);
	panel1.add(label17);panel1.add(attrib17);panel1.add(len17);panel1.add(prec17);
	panel1.add(label18);panel1.add(attrib18);panel1.add(len18);panel1.add(prec18);
	panel1.add(label19);panel1.add(attrib19);panel1.add(len19);panel1.add(prec19);
	panel2.add(Ok);
	panel1.add(label20);panel1.add(attrib20);panel1.add(len20);panel1.add(prec20);
	getContentPane().add(panel1,BorderLayout.CENTER);
	getContentPane().add(panel2,BorderLayout.SOUTH);
  }
}
class Arrow extends Tool {

  Arrow() { // undo measure tool residue
    QuickStartXYTool2.milesLabel.setText("DIST   0 mi   ");
    QuickStartXYTool2.kmLabel.setText("   0 km    ");
    //QuickStartXYTool2.map.remove(QuickStartXYTool2.acetLayer);
    QuickStartXYTool2.acetLayer = null;
    QuickStartXYTool2.map.repaint();
  }
}
class Flash extends Thread {
  Legend legend;
  Flash(Legend legendin) {
	legend = legendin;
  }
  public void run() {
	for (int i=0;i<12;i++) {
	  try {
		Thread.sleep(500);
		legend.toggleSelected();
	  } catch (Exception e) {}
    }
  }
}
class DistanceTool extends DragTool  {
  int startx,starty,endx,endy,currx,curry;
  com.esri.mo2.cs.geom.Point initPoint, endPoint, currPoint;
  double distance;
  public void mousePressed(MouseEvent me) {
	startx = me.getX(); starty = me.getY();
	initPoint = QuickStartXYTool2.map.transformPixelToWorld(me.getX(),me.getY());
  }
  public void mouseReleased(MouseEvent me) {
	  // now we create an acetatelayer instance and draw a line on it
	endx = me.getX(); endy = me.getY();
	endPoint = QuickStartXYTool2.map.transformPixelToWorld(me.getX(),me.getY());
    distance = (69.44 / (2*Math.PI)) * 360 * Math.acos(
				 Math.sin(initPoint.y * 2 * Math.PI / 360)
			   * Math.sin(endPoint.y * 2 * Math.PI / 360)
			   + Math.cos(initPoint.y * 2 * Math.PI / 360)
			   * Math.cos(endPoint.y * 2 * Math.PI / 360)
			   * (Math.abs(initPoint.x - endPoint.x) < 180 ?
                    Math.cos((initPoint.x - endPoint.x)*2*Math.PI/360):
                    Math.cos((360 - Math.abs(initPoint.x - endPoint.x))*2*Math.PI/360)));
    System.out.println( distance  );
    QuickStartXYTool2.milesLabel.setText("DIST: " + new Float((float)distance).toString() + " mi  ");
    QuickStartXYTool2.kmLabel.setText(new Float((float)(distance*1.6093)).toString() + " km");
    if (QuickStartXYTool2.acetLayer != null)
      QuickStartXYTool2.map.remove(QuickStartXYTool2.acetLayer);
    QuickStartXYTool2.acetLayer = new AcetateLayer() {
      public void paintComponent(java.awt.Graphics g) {
		java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
		Line2D.Double line = new Line2D.Double(startx,starty,endx,endy);
		g2d.setColor(new Color(0,0,250));
		g2d.draw(line);
      }
    };
    Graphics g = super.getGraphics();
    QuickStartXYTool2.map.add(QuickStartXYTool2.acetLayer);
    QuickStartXYTool2.map.redraw();
  }
  public void cancel() {};
}
