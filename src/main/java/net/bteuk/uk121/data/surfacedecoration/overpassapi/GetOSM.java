package net.bteuk.uk121.data.surfacedecoration.overpassapi;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.bteuk.uk121.mod.UK121;
import net.bteuk.uk121.util.geometry.BoundingBox;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GetOSM extends DefaultHandler
{
	private static ArrayList<Object> objects = new ArrayList<Object>();
	private static BoundingBox bbox;

	private static ArrayList<Way> ways;
	private static ArrayList<Node> nds = new ArrayList<Node>();

	public static String directory = UK121.directory + "Ways/";
	private static boolean bOnWay;

	String[] DataReturned;
	
	boolean testValueVaryWeather = true;

	public GetOSM(double latMin, double longMin, double latMax, double longMax)
	{
		bbox = new BoundingBox(latMin, latMax, longMin, longMax);
	}

	public GetOSM(BoundingBox bbox)
	{
		this.bbox = bbox;
	}

	public static ArrayList<Way> loadTiles(BoundingBox bbox)
	{
		Calendar cal = Calendar.getInstance();
		Date time = cal.getTime();
		long lTime1 = time.getTime();

		ways = new ArrayList<Way>();
		try
		{
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setErrorHandler(new MyErrorHandler(System.err));

			xmlReader.setContentHandler((ContentHandler) new GetOSM(bbox));
			String URL = "https://overpass.kumi.systems/api/interpreter?data=%5Btimeout%3A5%5D%3B%0A%28" +
					"%0A%20%20nwr%5Bbuilding%5D%28" +bbox.minX()+"%2C"+bbox.minZ()+"%2C" +
					bbox.maxX() +"%2C"+bbox.maxZ()+"%29%3B%0A%20%20nwr%5Bhighway%5D%28" +bbox.minX()+"%2C"+bbox.minZ()+"%2C" +
					bbox.maxX() +"%2C"+bbox.maxZ()+ "%29%3B" +
					"%0A%29%3B" +
					"%0Aout%20ids%3B";
			System.out.println(URL);
			xmlReader.parse(URL);

			cal = Calendar.getInstance();
			time = cal.getTime();
			long lTime2 = time.getTime();

			System.out.println("IDs api call: " +(lTime2-lTime1) +"ms");

			File file;
			Scanner infile;
			String szInLine;
			String[] coords;
			Node node;
			boolean bAPI = false;

			//Goes through all of the ways
			for (int i = 0 ; i < ways.size() && bAPI == false; i++)
			{
				try
				{
					file = new File(directory+ways.get(i).id+".txt");
					if (file.exists())
					{
						infile = new Scanner(file);
						//Skips the header row
						szInLine = infile.nextLine();

						//Finds out how many schools there are
						while (infile.hasNext())
						{
							szInLine = infile.nextLine();
							coords = szInLine.split(",");
							node = new Node(Long.parseLong(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
						}
						infile.close();
					}
					else
					{
						System.out.println("Initiating API fetch:");

						cal = Calendar.getInstance();
						time = cal.getTime();
						lTime1 = time.getTime();

						ways = entry(bbox, false);

						cal = Calendar.getInstance();
						time = cal.getTime();
						lTime2 = time.getTime();

						System.out.println("Fetching ALL data from the API: " +(lTime2-lTime1) +"ms");

						bAPI = true;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (SAXException e)
		{
			UK121.LOGGER.error("SAX Exception error OSM data: \n"+e.getMessage());
			//	e.printStackTrace();
		}
		catch (IOException e)
		{
			UK121.LOGGER.error("IO Exception error OSM data \n"+e.getMessage());
			//	e.printStackTrace();
		}
		catch (Exception e)
		{
			UK121.LOGGER.error("Exception error OSM data");
			e.printStackTrace();
		}
		return ways;
	}

	public static ArrayList<Way> entry(BoundingBox bbox, boolean bAlternative)
	{
		ways = new ArrayList<Way>();
		try
		{
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setErrorHandler(new MyErrorHandler(System.err));

			xmlReader.setContentHandler((ContentHandler) new GetOSM(bbox));
			String URL = "";

			if (bAlternative)
			{
				URL = URL + "https://z.overpass-api.de/api/interpreter?data=%5Btimeout%3A5%5D%3B%0A%28";
			}
			else
			{
				URL = URL + "https://overpass.kumi.systems/api/interpreter?data=%5Btimeout%3A5%5D%3B%0A%28";
			}
			URL = URL + "%0A%20%20nwr%5Bbuilding%5D%28" +bbox.minX()+"%2C"+bbox.minZ()+"%2C" +
					bbox.maxX() +"%2C"+bbox.maxZ()+"%29%3B%0A%20%20nwr%5Bhighway%5D%28" +bbox.minX()+"%2C"+bbox.minZ()+"%2C" +
					bbox.maxX() +"%2C"+bbox.maxZ()+ "%29%3B" +
					"%0A%29%3B" +
					"%0Aout%20geom%3B";
			System.out.println(URL);
			xmlReader.parse(URL);

			File file;
			Scanner infile;
			String szInLine;
			Node node;

			FileWriter fw;
			BufferedWriter bw;

			//Adds ways to cache
			for (int i = 0 ; i < ways.size() ; i++)
			{
				try
				{
					file = new File(directory+ways.get(i).id+".txt");
					if (file.exists())
					{
					}
					else
					{
						//Creates directory and file
						file.getParentFile().mkdirs();
						file.createNewFile();

						fw = new FileWriter(directory+ways.get(i).id+".txt", true);
						bw = new BufferedWriter(fw);
						bw.write(ways.get(i).id+"\n");
						for (int j = 0 ; j < ways.get(i).nodes.size() ; j++)
						{
							szInLine = ""+ways.get(i).nodes.get(j).ref+","
									+ways.get(i).nodes.get(j).latitude+","
									+ways.get(i).nodes.get(j).longitude;
							bw.write(szInLine+"\n");
						}
						bw.close();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (SAXException e)
		{
			UK121.LOGGER.error("SAX Exception error OSM data: \n"+e.getMessage());
		//	e.printStackTrace();
		}
		catch (IOException e)
		{
			UK121.LOGGER.error("IO Exception error OSM data \n"+e.getMessage());
		//	e.printStackTrace();
		}
		catch (Exception e)
		{
			UK121.LOGGER.error("Exception error OSM data");
			e.printStackTrace();
		}
		return ways;
	}

	public void startDocument() throws SAXException
	{

	}

	public void startElement(String namespaceURI, String localName, String qName,  Attributes atts) throws SAXException
	{
		Section section;
		Way way;
		Node nd;

		int i;
		int iAttributes = atts.getLength();
		bOnWay = false;

		//Checks the element name for way
		if (localName.equals("way"))
		{
			way = new Way();
			//Goes through the attributes of the way until it finds id
			for (i = 0 ; i < iAttributes ; i++)
			{
				if (atts.getLocalName(i).equals("id"))
				{
					way.id = atts.getValue(i);
				}
			}
			ways.add(way);
			bOnWay = true;
		}
		else if (localName.equals("tag") && bOnWay)
		{
			Tag tag;

			//Creates new tag and adds it to the array
			tag = new Tag(atts.getValue(0), atts.getValue(1));
			//Adds that tax to the tag list for the current way
			ways.get((ways.size()-1)).tags.add(tag);
		}

		//Checks the element name for nd
		else if (localName.equals("nd") && bOnWay)
		{
			long lRef = 0;
			double lat = 0;
			double lon = 0;

			//Goes through the attributes of the way until it finds ref, lat and long
			for (i = 0 ; i < iAttributes ; i++)
			{
			//	System.out.println("Member "+i+": " + atts.getLocalName(i) +" = " +atts.getValue(i));
				switch (atts.getLocalName(i))
				{
					case "ref":
						lRef = Long.parseLong(atts.getValue(i));
						break;
					case "lon":
						lon = Double.parseDouble(atts.getValue(i));
						break;
					case "lat":
						lat = Double.parseDouble(atts.getValue(i));
				}
			}
			nd = new Node(lRef, lat, lon);
			ways.get((ways.size()-1)).nodes.add(nd);
		}
	}
	
	public void endDocument() throws SAXException
	{
		
	}

	public static void main(String[] args)
	{
		try
		{
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setErrorHandler(new MyErrorHandler(System.err));

			xmlReader.setContentHandler((ContentHandler) new GetOSM(51.43801,0.38390, 51.43805,0.38395));
			String URL = "https://overpass.kumi.systems/api/interpreter?data=%5Btimeout%3A1%5D%3B%0A%28" +
					"%0A%20%20nwr%5Bbuilding%5D%28" +bbox.minX()+"%2C"+bbox.minZ()+"%2C" +
					bbox.maxX() +"%2C"+bbox.maxZ()+"%29%3B%0A%20%20nwr%5Bhighway%5D%28" +bbox.minX()+"%2C"+bbox.minZ()+"%2C" +
					bbox.maxX() +"%2C"+bbox.maxZ()+ "%29%3B" +
					"%0A%29%3B" +
					"%0Aout%20geom%3B";
			System.out.println(URL);
			xmlReader.parse(URL);
			//		https://overpass-api.de/api/interpreter?data=%28%0A%20%20nwr%5Bbuilding%5D%2851.43722966914172%2C0.38277268409729004%2C51.438349904151565%2C0.3850364685058594%29%3B%0A%20%20nwr%5Bhighway%5D%2851.43722966914172%2C0.38277268409729004%2C51.438349904151565%2C0.3850364685058594%29%3B%0A%29%3B%0Aout%20geom%3B
			//		https://overpass-api.de/api/interpreter?data=nwr%5Bbuilding%5D%2851.43644372195998%2C0.3816638279204503%2C51.4381892499097%2C0.38544040834318255%29%3B%0A%20%20nwr%5Bhighway%5D%2851.43644372195998%2C0.3816638279204503%2C51.4381892499097%2C0.38544040834318255%29%3B%0A%29%3B%0Aout%20geom%3B
			//Adds all of the ways to the object array
			for (int i = 0 ; i < ways.size() ; i++)
			{
			//	objects.add(ways.get(i));
			}

			//Displays all the objects
		/*	for (int i = 0 ; i < objects.size() ; i++)
			{
				if (objects.get(i) instanceof Way)
				{
					Way way = (Way) objects.get(i);
					System.out.println("\nWay: "+way.id);
					for (int j = 0 ; j < way.tags.size() ; j++)
					{
						System.out.println("---New tag---");
						System.out.println(way.tags.get(j).key +" = " +way.tags.get(j).value);
					}
					for (int j = 0 ; j < way.nodes.size() ; j++)
					{
						System.out.println("---New node---");
						System.out.println("Ref: "+way.nodes.get(j).ref);
						System.out.println("Latitude: "+way.nodes.get(j).latitude);
						System.out.println("Longitude: "+way.nodes.get(j).longitude);
					}
				}
			}
		 */
		}
		catch (SAXException e)
		{
			//	UK121.LOGGER.error("SAX Exception error OSM data");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			//	UK121.LOGGER.error("SAX Exception error OSM data");
			e.printStackTrace();
		}
		catch (Exception e)
		{
			//	UK121.LOGGER.error("SAX Exception error OSM data");
			e.printStackTrace();
		}
	}
}

class MyErrorHandler implements ErrorHandler
{
	private PrintStream out;

	MyErrorHandler(PrintStream out)
	{
		this.out = out;
	}

	private String getParseExceptionInfo(SAXParseException spe)
	{
		String systemId = spe.getSystemId();

		if (systemId == null) {
			systemId = "null";
		}

		String info = "URI=" + systemId + " Line=" 
				+ spe.getLineNumber() + ": " + spe.getMessage();

		return info;
	}

	public void warning(SAXParseException spe) throws SAXException {
		out.println("Warning: " + getParseExceptionInfo(spe));
	}

	public void error(SAXParseException spe) throws SAXException {
		String message = "Error: " + getParseExceptionInfo(spe);
		throw new SAXException(message);
	}

	public void fatalError(SAXParseException spe) throws SAXException {
		String message = "Fatal Error: " + getParseExceptionInfo(spe);
		throw new SAXException(message);
	}
}

