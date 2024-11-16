package com.Resume;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import io.nayuki.qrcodegen.QrCode;


public class Resume  {
	Font titleFont; //This is the font that will be used within the title
	Font contactInfoFont; //The personal info such as git/email/etc
	Font sectionFont; //The large 
	Font subsectionFont; //The entries in each section
	Font subsectionDescriptorFont; //The line under the subsection (Mainly for education portion)
	Font bulletFont; //This is the font that will be used for any bulleted information.
	Font zapFont; //This is the zapfdingbat font that is used to create bullet points.
	Font dateLocationFont; //The font any date of employment or location of employment will use
	
	//The padding between each section.
	float sectionPadding = 10f;
	//The padding between each subsection 
	float subsectionPadding = 5f;
	
	//The font path 
	private final static String FONTPATH = "fonts";
	private final static String REPOURI = "https://github.com/StevenStreasick/Resume/blob/main/README.md";
	/**
	 * Sets the fonts that will be used
	 * 
	 * @return {@code true} if the fonts were set successfully 
	 */
	//@return success
	private boolean createFonts() {
		//First register font. Then
		try {
			BaseFont fontToUse = BaseFont.createFont(FONTPATH + File.separator + "Roboto-Light.ttf", BaseFont.CP1250, BaseFont.EMBEDDED); 
			
			titleFont = new Font(fontToUse, 24, Font.BOLD, Color.black); 
			contactInfoFont = new Font(fontToUse, 10, Font.NORMAL, Color.black);
			sectionFont = new Font(fontToUse, 14, Font.BOLD, Color.black); 
			subsectionFont = new Font(fontToUse, 10.5f, Font.NORMAL, Color.black);
			subsectionDescriptorFont = new Font(fontToUse, subsectionFont.getSize() - 2, Font.ITALIC, subsectionFont.getColor());
			bulletFont = new Font(fontToUse, 10, Font.NORMAL, Color.black);
			zapFont = new Font(Font.ZAPFDINGBATS, bulletFont.getSize() - 6, bulletFont.getStyle(), bulletFont.getColor());
			dateLocationFont = new Font(fontToUse, 9, Font.NORMAL, Color.gray);
			
			return true;
			
		} catch (DocumentException de) { 
			de.printStackTrace();
		} catch (IOException ioe) {
			//Likely just a could not find the file error
			ioe.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Generates a QR code to the given URI
	 * 
	 * @param URI The identifier to store in the QR code
	 * @return {@code BufferedImage} storing the QR code
	 */
	private static BufferedImage createQRCode(String URI) {
		 int scalar = 16;
		 
		 QrCode.Ecc errorCorrectionLevel = QrCode.Ecc.LOW;
	     QrCode qrCode = QrCode.encodeText(REPOURI, errorCorrectionLevel);
         
	     int matrixSize = qrCode.size * scalar;
         
	     BufferedImage image = new BufferedImage(matrixSize, matrixSize, BufferedImage.TYPE_INT_RGB);
         image.createGraphics();
         
         //-----Fill the background with white------
         Graphics2D graphics = (Graphics2D) image.getGraphics();
         graphics.setColor(Color.WHITE);
         graphics.fillRect(0, 0, matrixSize, matrixSize);
         
         
         //------Add the black dots to make up the QR code
         graphics.setColor(Color.BLACK);

         for (int x = 0; x < qrCode.size; x++) {
             for (int y = 0; y < qrCode.size; y++) {
                 if (!qrCode.getModule(x, y)) { continue; }
                 
                 graphics.fillRect(x * scalar, y * scalar, scalar, scalar);
              
             }
         }
         
		 return image;
	}
	
	
	/**
	 * Creates a new title of a new section containing title formatting
	 * as a {@code PdfPCell} 
	 * @param rawText The text to display
	 * @return {@code PdfPCell} containing section formatting
	 */
	private PdfPCell createSectionCell(String rawText) {
		//The text to display.
		String titleText = rawText.toUpperCase();
		
		//Creates a chunk so that the font width can be adjusted
		Chunk titleChunk = new Chunk(titleText, sectionFont);
		//Adjust the stroke width to be smaller. 	
		titleChunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL, .05f, sectionFont.getColor());
		
		//Create the PdfPCell from the chunk
		PdfPCell title = new PdfPCell(new Phrase(titleChunk));
		//Format the PdfPCell
		title.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		title.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		title.setBorder(PdfPCell.NO_BORDER);
		title.setPaddingBottom(sectionPadding / 2f);
		
		//Return the PdfPCell
		return title;
	}
	
	/**
	 * Takes in a given text and bullets it.
	 * 
	 * @param text The text to display
	 * @return {@code PdfPCell} containing the bulleted text
	 */
	private PdfPCell createBulletCell(String text) {
		//NOTE: I chose to use this chunk method over fontSelector so I can utilize multiple font sizes.
		
		//Create a bullet 
		Chunk bullet = new Chunk(String.valueOf((char) 108) + "  ", zapFont);
		//Create the text
		Chunk textChunk = new Chunk(text, bulletFont);
		//Create a container so that both the bullet and the text can be added to it.
		Paragraph paragraph = new Paragraph();
		
		//The bullet font is smaller. Raise it up to center it. 
		bullet.setTextRise(2f);
		
		//Add the bullet/text to the container
		paragraph.add(bullet);
		paragraph.add(textChunk);
		
		//Create a PdfPCell that will format the container
		PdfPCell bulletCell = new PdfPCell(paragraph);
		//Format the PdfPCell
		bulletCell.setBorder(PdfPCell.NO_BORDER);
		bulletCell.setUseAscender(true);
		bulletCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		bulletCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		bulletCell.setPaddingBottom(4f);
		
		//Return the PdfPCell
		return bulletCell;
	}
	
	/**
	 * Creates a subsection header and returns a {@code PdfPTable} container 
	 * holding the subsection info
	 * 
	 * @param subsectionTitle The title to display
	 * @param subsectionDate The date to display
	 * @param subsectionDesc The description to display below the title. Can be null
	 * @return {@code PdfPTable} 
	 */
	private PdfPTable createSubsectionTable(String subsectionTitle, String subsectionDate, String subsectionDesc) {
		//The experience container
		PdfPTable experienceHeader = new PdfPTable(2);
		
		//Pads the container
		experienceHeader.setSpacingBefore(3);
		experienceHeader.setSpacingAfter(3);
		
		//Creates the title to the subsection
		PdfPCell subsectionTitleCell = new PdfPCell(new Phrase(subsectionTitle, subsectionFont));
		//Formats the title to the subsection
		subsectionTitleCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		subsectionTitleCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		subsectionTitleCell.setBorder(PdfPCell.NO_BORDER);
		subsectionTitleCell.setPaddingLeft(0);
		subsectionTitleCell.setUseAscender(true);
		
		//Creates the date to the subsection
		PdfPCell subsectionDateCell = new PdfPCell(new Phrase(subsectionDate, dateLocationFont));
		//Formats the date to the subsection
		subsectionDateCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		subsectionDateCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		subsectionDateCell.setBorder(PdfPCell.NO_BORDER);
		subsectionDateCell.setPaddingRight(0);
		subsectionDateCell.setUseAscender(true);
		
		//Adds the title/date to the experience
		experienceHeader.addCell(subsectionTitleCell);
		experienceHeader.addCell(subsectionDateCell);
		
		//Try to create a description to the experience
		if(subsectionDesc != null && !subsectionDesc.isBlank()) {
			//Creates the description to the subsection
			PdfPCell subsectionDescCell = new PdfPCell(new Phrase(subsectionDesc, subsectionDescriptorFont));
			//Formats the description to the subsection
			subsectionDescCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			subsectionDescCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			subsectionDescCell.setBorder(PdfPCell.NO_BORDER);
			subsectionDescCell.setPaddingLeft(0);
			subsectionDescCell.setUseAscender(true);
			subsectionDescCell.setColspan(2);
			
			//Adds the description to the experience
			experienceHeader.addCell(subsectionDescCell);
		}
		
		return experienceHeader;
	}
	/**
	 * Creates a header to the resume
	 * 
	 * @return {@code PdfPTable} containing the header info
	 */
	private PdfPTable createHeader() {
		
		//The header container
		PdfPTable header = new PdfPTable(3);
		header.setWidthPercentage(100f);
		header.setWidths(new float[] {1f, .5f, 1f});		
		header.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

		//Title column
		PdfPTable leftColumn = new PdfPTable(1);
		leftColumn.setWidthPercentage(100f);
		
		//QR code column
		PdfPTable qrcodeColumn = new PdfPTable(1);
		qrcodeColumn.setWidthPercentage(100f);
		
		//The contact info column (Phone #, address, LinkedIn)
		PdfPTable rightColumn = new PdfPTable(1);
		rightColumn.setWidthPercentage(100f);
		
		//Creates a chunk containing my name
		Chunk name = new Chunk("Steven Streasick", titleFont);
		//Reduces stroke width
		name.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL, .01f, sectionFont.getColor());
		
		//Creates a container for my name
		PdfPCell title = new PdfPCell(new Phrase(name));
		//Formats the name container
		title.setBorder(PdfPCell.NO_BORDER);
		title.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		title.setVerticalAlignment(PdfPCell.ALIGN_TOP);
		title.setPaddingBottom(6f);
		title.setPaddingLeft(0);
		title.setUseAscender(true);
		
		//Creates the email
		PdfPCell email = new PdfPCell(new Phrase("StevenStreasick@gmail.com", contactInfoFont));
		//Formats the email
		email.setBorder(PdfPCell.NO_BORDER);
		email.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		email.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		email.setPaddingBottom(6f);
		email.setPaddingLeft(0);
		email.setUseAscender(true);
		
		//Create an anchor for the gitURL
		Anchor gitURL = new Anchor("GitHub.com/StevenStreasick", contactInfoFont);
		//Set the anchor's name
		gitURL.setName("Git");
		//Set the hyperlink
		gitURL.setReference("https://GitHub.com/StevenStreasick");
		
		//Create a container for the anchor
		PdfPCell git = new PdfPCell(gitURL);
		//Format the anchor container.
		git.setBorder(PdfPCell.NO_BORDER);
		git.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		git.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		git.setPadding(0);
		git.setUseAscender(true);
		
		//Create a byteArrayOutputStream to write the QR code to.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//Get the qrCode
		BufferedImage qrCodeBuffImage = createQRCode(REPOURI);
		//Create a container to hold the QR code
		PdfPCell qrCode = new PdfPCell(new Phrase(""));
		
		try {
		
			//Try to write the qrcode to the BAOS as a jpg
			ImageIO.write(qrCodeBuffImage, "jpg", baos);
			//Convert the baos to a byte array to an Image
			Image qrCodeImage = Image.getInstance(baos.toByteArray());
			
			//Scale the QR code. Create the QR code big, then scale down for clarity reasons. 
			qrCodeImage.scalePercent(8.6f);
			//Add the QR code image to the container
			qrCode = new PdfPCell(qrCodeImage);
			
		} catch (Exception e) {
			
			e.printStackTrace();
		
		}
		
		//Format the QR code container
		qrCode.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		qrCode.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
		qrCode.setBorder(PdfPCell.NO_BORDER);
		qrCode.setPaddingTop(4f);
		
		//Create my phone number
		PdfPCell phoneNumber = new PdfPCell(new Phrase("231-357-5974", contactInfoFont));
		//Format my phone number
		phoneNumber.setBorder(PdfPCell.NO_BORDER);
		phoneNumber.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		phoneNumber.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		phoneNumber.setPaddingTop(12f);
		phoneNumber.setPaddingRight(0f);
		phoneNumber.setPaddingBottom(5.1f);
		phoneNumber.setUseAscender(true);
		
		//Create my address
		PdfPCell address = new PdfPCell(new Phrase("Allendale, MI 49401", contactInfoFont));
		//Format my address
		address.setBorder(PdfPCell.NO_BORDER);
		address.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		address.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		address.setPaddingRight(0);
		address.setPaddingBottom(5.1f); 
		address.setUseAscender(true);
				
		//Create an anchor for the LinkedIn
		Anchor linkedinURL = new Anchor("LinkedIn.com/in/Steven-Streasick", contactInfoFont);
		//Set the anchor's name
		linkedinURL.setName("LinkedIn");
		//Set the hyperlink
		linkedinURL.setReference("Https://LinkedIn.com/in/Steven-Streasick");
		
		//Create a container for the anchor
		PdfPCell linkedin = new PdfPCell(linkedinURL);
		//Format the LinkedIn
		linkedin.setBorder(PdfPCell.NO_BORDER);
		linkedin.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		linkedin.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		linkedin.setPaddingRight(0);
		linkedin.setPaddingBottom(0f);
		linkedin.setUseAscender(true);
		
		//Create a cell which will be stretched instead of the rest of the column.
		PdfPCell paddingCell = new PdfPCell(new Phrase(""));
		paddingCell.setBorder(PdfPCell.NO_BORDER);
		
		//Setup the left column
		leftColumn.addCell(title);
		leftColumn.addCell(email);
		leftColumn.addCell(git);
		leftColumn.addCell(paddingCell);
		
		//Add the left column
		header.addCell(leftColumn);
		
		//Setup the middle column
		qrcodeColumn.addCell(qrCode);
		qrcodeColumn.addCell(paddingCell);
		
		//Add the middle column
		header.addCell(qrcodeColumn);
		
		//Setup the right column
		rightColumn.addCell(phoneNumber);
		rightColumn.addCell(address);
		rightColumn.addCell(linkedin);
		rightColumn.addCell(paddingCell);
		
		//Add the right column
		header.addCell(rightColumn);
		
		return header;
	}
	
	
	/**
	 * Creates the education section on the resume
	 * 
	 * @return {@code PdfPTable} containing the education info
	 */
	private PdfPTable createEducationSection() {
		//The education section container
		PdfPTable educationSection = new PdfPTable(1);
		educationSection.setWidthPercentage(100f);
		educationSection.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		educationSection.setSpacingBefore(sectionPadding / 3);
		educationSection.setSpacingAfter(sectionPadding);
		
		//The header container for this section
		PdfPTable educationTable = new PdfPTable(3);
		educationTable.setWidthPercentage(100f);
		educationTable.setWidths(new float[] {.92f, 1f, .32f});
		
		//The title to this section
		PdfPCell educationTitle = createSectionCell("Education");
		
		//Create the university info
		PdfPCell schoolCell = new PdfPCell(new Phrase("Grand Valley State University, Padnos College of Engineering and Computing | Honors College", 
				subsectionFont));
		//Format the university info
		schoolCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		schoolCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		schoolCell.setColspan(2);
		schoolCell.setBorder(PdfPCell.NO_BORDER);
		schoolCell.setPaddingLeft(0);
		schoolCell.setUseAscender(true);
		
		//Create the location info
		PdfPCell location = new PdfPCell(new Phrase("Allendale, MI", dateLocationFont));
		//Format location info
		location.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		location.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		location.setBorder(PdfPCell.NO_BORDER);
		location.setPaddingRight(0);
		location.setUseAscender(true);
		
		//Create degree info
		PdfPCell degree = new PdfPCell(new Phrase("Masters of Science, Computer Science", subsectionDescriptorFont));
		//Format degree info
		degree.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		degree.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		degree.setBorder(PdfPCell.NO_BORDER);
		degree.setPaddingLeft(0);
		degree.setUseAscender(true);
		
		//Create minor info
		PdfPCell minor = new PdfPCell(new Phrase("Minor in Mathematics", subsectionDescriptorFont));
		//Format minor info
		minor.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		minor.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		minor.setBorder(PdfPCell.NO_BORDER);
		minor.setUseAscender(true);
		minor.setColspan(2);
		
		//Add bulleted info
		PdfPCell bullet1 = createBulletCell("GPA of 3.860");
		
		PdfPCell bullet2 = createBulletCell("Dean's List: Fall 2022, Winter 2022, Fall 2023, Winter 2023");
		
		PdfPCell bullet3 = createBulletCell("Relevant Classwork: Datastructures and Algorithms, Applied AI, "
				+ "Software Engineering, Linear Algebra");
		
		//Setup the header
		educationTable.addCell(schoolCell);
		educationTable.addCell(location);
		educationTable.addCell(degree);
		educationTable.addCell(minor);
		
		//Add 'Education' to table
		educationSection.addCell(educationTitle);
		//Add the header
		educationSection.addCell(educationTable);
		//Add bullet info
		educationSection.addCell(bullet1);
		educationSection.addCell(bullet2);
		educationSection.addCell(bullet3);
		
		return educationSection;
	}
	/**
	 * Creates the experience section. This contains previous job positions that I have held.
	 * 
	 * @return {@code PdfPTable} containing the experience info
	 */
	private PdfPTable createExperienceSection() {
		//The experience section container
		PdfPTable experience = new PdfPTable(1);
		experience.setWidthPercentage(100f);
		experience.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		experience.setSpacingAfter(sectionPadding);
		
		//Create a experience section cell
		PdfPCell experienceTitle = createSectionCell("EXPERIENCE");
		
		//Create a EJ software dev section 
		PdfPTable experience1Header = createSubsectionTable("EJ Full Stack Software Developer", "May 2024 – August 2024", null);
		
		//Add software dev bullets
		PdfPCell ejSWDBullet1 = createBulletCell("Developed and optimized PDF solutions using OpenPDF in Java, streamlining processes "
				+ "in Salesforce");
		PdfPCell ejSWDBullet2 = createBulletCell("Identified and resolved bugs in OpenPDF, contributing pull requests to improve "
				+ "functionality");
		PdfPCell ejSWDBullet3 = createBulletCell("Improved Salesforce processes with custom Java solutions for better data management "
				+ "and workflow efficiency");
		
		//Create a freelance programmer section
		PdfPTable experience2Header = createSubsectionTable("Freelance Programmer", "August 2015 – Now", null);
		
		//Add freelance prog. bullets
		PdfPCell flpBullet1 = createBulletCell("Proactively stayed at the forefront of Roblox’s ever-changing landscape, consistently "
				+ "integrating leading technologies");
		PdfPCell flpBullet2 = createBulletCell("Completed various Hidden Developer Contracts, showcasing integrity while fostering relations "
				+ "within the community");
		
		//Add Pantry Chef section
		PdfPTable experience3Header = createSubsectionTable("Cafe Santé Head Pantry Chef", "June 2022 - August 2023", null);
		
		//Add pantry chef bullets
		PdfPCell pcBullet1 = createBulletCell("Reduced overall ticket time for pantry orders by 13%");
		PdfPCell pcBullet2 = createBulletCell("Led six pantry cooks, ensuring clear communication and efficient operations for consistent customer experiences");
		
		//Set the padding between subsections
		ejSWDBullet3.setPaddingBottom(subsectionPadding);
		flpBullet2.setPaddingBottom(subsectionPadding);

		//Add 'Experience' 
		experience.addCell(experienceTitle);
		
		//Add the SW dev stuff
		experience.addCell(experience1Header);
		experience.addCell(ejSWDBullet1);
		experience.addCell(ejSWDBullet2);
		experience.addCell(ejSWDBullet3);
		
		//Add freelance programmer stuff
		experience.addCell(experience2Header);
		experience.addCell(flpBullet1);
		experience.addCell(flpBullet2);
		
		//Add pantry chef stuff
		experience.addCell(experience3Header);
		experience.addCell(pcBullet1);
		experience.addCell(pcBullet2);

		return experience;
	}
	
	/**
	 * Creates the projects section. 
	 * 
	 * @return {@code PdfPCell} containing project info
	 */
	private PdfPTable createProjects() {
		//The projects section container
		PdfPTable projects = new PdfPTable(1);
		projects.setWidthPercentage(100f);
		projects.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		projects.setSpacingAfter(sectionPadding);
		
		//Creates a 'Projects' title
		PdfPCell projectsTitle = createSectionCell("Projects");
		
		//Add mutation testing plugin subsection
		PdfPTable project1Header = createSubsectionTable("Mutation Testing Plugin", "Jaunary 2023 - April 2023", "Roblox testing tool");
		
		//Add mutation testing plugin bullets
		PdfPCell mtBullet1 = createBulletCell("Assessed effectiveness of mutation testing in game development, gauging community opinion on its utility and impact");
		PdfPCell mtBullet2 = createBulletCell("Developed a code injection framework for consistent reference handling amid script hierarchical changes");
		PdfPCell mtBullet3 = createBulletCell("Employed string manipulation in Lua to simulate code mutations, optimizing bug detection and debugging");
		
		//Create a minesweeper subsection
		PdfPTable project2Header = createSubsectionTable("Minesweeper", "January 2022 - April 2023", "Unpublished Roblox video game");
		
		//Add minesweeper bullet info
		PdfPCell msweepBullet1 = createBulletCell("Created a three dimensional version of the classic game Minesweeper using complex garbage collection techniques");
		PdfPCell msweepBullet2 = createBulletCell("Utilized access modifiers to control access to class data, promoting data integrity");
		
		//Create a coloring sim subsection
		PdfPTable project3Header = createSubsectionTable("Coloring Simulator", "August 2018 - July 2022", "Roblox video game with ~450k plays");
		
		//Add coloring sim bullet info
		PdfPCell colSimBullet1 = createBulletCell("Designed a framework that allowed for other programmers to expand upon");
		PdfPCell colSimBullet2 = createBulletCell("Learned to design achievable deadlines through precise planning and coordination with several developers");
		
		//Create a chess subsection
		PdfPTable project4Header = createSubsectionTable("Chess", "August 2022 - October 2022", "Java based school project");
		
		//Add chess bullet info
		PdfPCell chessBullet1 = createBulletCell("Reduced code duplication by employing inheritance to create a hierarchy of game elements");
		PdfPCell chessBullet2 = createBulletCell("Implemented an AI opponent that prioritizes piece protection and strategic piece captures");

		//Set padding between subsections
		colSimBullet2.setPaddingBottom(subsectionPadding);
		msweepBullet2.setPaddingBottom(subsectionPadding);
		chessBullet2.setPaddingBottom(subsectionPadding);
		
		//Add 'Projects'
		projects.addCell(projectsTitle);
		
		//Add the mutation testing stuff
		projects.addCell(project1Header);
		projects.addCell(mtBullet1);
		projects.addCell(mtBullet2);
		projects.addCell(mtBullet3);
		
		//Add the minesweeeper stuff
		projects.addCell(project2Header);
		projects.addCell(msweepBullet1);
		projects.addCell(msweepBullet2);
		
		//Add the coloring Simulator stuff
		projects.addCell(project3Header);
		projects.addCell(colSimBullet1);
		projects.addCell(colSimBullet2);
		
		//Add the chess stuff
		projects.addCell(project4Header);
		projects.addCell(chessBullet1);
		projects.addCell(chessBullet2);
	
		return projects;
	}
	
	/**
	 * Create a Skills section
	 * 
	 * @return {@code PdfPCell} containing the skills info
	 */
	private PdfPTable createSkills() {
		Font skillsFont = bulletFont; //A reference. Controls the font used within this section
		
		//The skills section container
		PdfPTable skills = new PdfPTable(1);
		skills.setWidthPercentage(100f);
		skills.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		skills.setSpacingAfter(sectionPadding);
		
		//Creates 'Skills' title
		PdfPCell skillsTitle = createSectionCell("Skills");
		
		//The languages that will be displayed 
		String[] languagesToDisplay = {"Java","C++", "C", "Python", "Lua", "JavaScript", "SQL"};
		//The skills that will be displayed
		String[] skillsToDisplay = {"Functional Programming", "Object Oriented Programming", "Software Testing", "Never Nesting"};
		
		//The string/font that will be used in the languages container
		Phrase programmingLanguages = new Phrase("Programming Languages: ", skillsFont);
		
		//Iterate over all the languages, adding them to the programmingLanguages string
		for(int i = 0; i < languagesToDisplay.length; i++) {
			String language = languagesToDisplay[i];
			
			//Create a chunk which can then be added to programmingLanguages
			Chunk languageChunk;
			if(i + 1 < languagesToDisplay.length) {
				//More languages to display. Add a comma
				languageChunk = new Chunk(language + ", ", skillsFont);
			} else {
				//No more languages to display
				languageChunk = new Chunk(language, skillsFont);
			}
			
			//Add the chunk to the phrase
			programmingLanguages.add(languageChunk);
		}
		
		//The string/font that will be used in the skills container
		Phrase programmingSkills = new Phrase("Programming Skills: ", skillsFont);
		
		//Iterate over all of the skills, adding them to the programmingSkills string
		for(int i = 0; i < skillsToDisplay.length; i++) {
			String skill = skillsToDisplay[i];
			
			//Create a chunk which can then be added to programmingSkills
			Chunk skillChunk;
			if(i + 1 < skillsToDisplay.length) {
				//More skills to display. Add a coma
				skillChunk = new Chunk(skill + ", ", skillsFont);
			} else {
				//No more skills to display
				skillChunk = new Chunk(skill, skillsFont);
			}
			
			//Add the chunk to the phrase
			programmingSkills.add(skillChunk);
		}
		
		//Create the languages cell
		PdfPCell languagesCell = new PdfPCell(programmingLanguages);
		//Format the cell
		languagesCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		languagesCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		languagesCell.setBorder(PdfPCell.NO_BORDER);
		languagesCell.setPaddingLeft(0);
		languagesCell.setPaddingBottom(subsectionPadding / 1.5f);
		languagesCell.setUseAscender(true);
		
		//Create the skills cell
		PdfPCell skillsCell = new PdfPCell(programmingSkills);
		//Format the cell
		skillsCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		skillsCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		skillsCell.setBorder(PdfPCell.NO_BORDER);
		skillsCell.setPaddingLeft(0);
		skillsCell.setUseAscender(true);
		
		//Add 'Skills'
		skills.addCell(skillsTitle);
		
		//Add the languages
		skills.addCell(languagesCell);
		//Add the skills
		skills.addCell(skillsCell);
		
		return skills;
	}
	
	/**
	 * Creates the volunteer section
	 * 
	 * @return {@code PdfPTable}
	 */
	private PdfPTable createVolunteerOpportunities() {
		//The volunteer section container
		PdfPTable volunteerOpportunities = new PdfPTable(1);
		volunteerOpportunities.setWidthPercentage(100f);
		volunteerOpportunities.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
		volunteerOpportunities.setSpacingAfter(sectionPadding);
		
		//Creates 'Volunteer Opportunities' title
		PdfPCell volunteerOpportunitiesTitle = createSectionCell("Volunteer Opportunities");
		
		//Create a Solar Spark subsection
		PdfPTable ejSolarSpark = createSubsectionTable("Solar Spark", " ", null);
		//Create bullet point info
		PdfPCell ssBullet1 = createBulletCell("Mentored 30 Middle School students to achieve their goal of raising $70,000 for solar panels");
		PdfPCell ssBullet2 = createBulletCell("Established and sustained a revered, annual tradition");
		PdfPCell ssBullet3 = createBulletCell("Produced a promotional video to aid in the marketing of the project");
	
		//Add 'Volunteer Opportunities'
		volunteerOpportunities.addCell(volunteerOpportunitiesTitle);
		
		//Add solar spark subsection
		volunteerOpportunities.addCell(ejSolarSpark);
		//Add solar spark bullet info
		volunteerOpportunities.addCell(ssBullet1);
		volunteerOpportunities.addCell(ssBullet2);
		volunteerOpportunities.addCell(ssBullet3);
		
		return volunteerOpportunities;
	}
	
	/**
	 * Creates and returns a resume copy as a ByteArrayInputStream
	 * 
	 * @return {@code ByteArrayInputStream} contains the resume
	 */
	public ByteArrayInputStream createResume() {
		//Create a new BAOS to hold the PDF
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//Setup the fonts
		createFonts();

		//Create a new document object
		Document doc = new Document(PageSize.A4, 34, 34, 38, 38);
		//Assign the doc to to the baos
		PdfWriter writer = PdfWriter.getInstance(doc, baos);

		doc.open();
		
		//Get the header section (Contains name/contact info)
		PdfPTable header = createHeader();
		//Add header section
		doc.add(header);
				
		//Get the height of the header
		float headerHeight = header.calculateHeights(false); //NOTE: adding header to the doc already calculates the height once, 
															 //      so this is not the first time calculating it.

		//Get the current layer
		PdfContentByte cb = writer.getDirectContent();	
		//Create a new horizontal line
		LineSeparator horzLine = new LineSeparator();
		//Draw the line from the left of the page to the right.
		horzLine.drawLine(cb, 
				doc.left(),  
				doc.right(), 
				doc.top() - headerHeight);
		
		//Add the education section
		doc.add(createEducationSection());

		//Add the experience section
		doc.add(createExperienceSection());
		
		//Add the projects section
		doc.add(createProjects());
		
		//Add the skills section
		doc.add(createSkills());
		
		//Add the volunteer opportunities section
		doc.add(createVolunteerOpportunities());
		
		doc.close();
		
		//Create a BAIS from the baos converted to byte[]
		return new ByteArrayInputStream(baos.toByteArray());
	}
}
