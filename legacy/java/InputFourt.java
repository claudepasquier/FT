import java.awt.*;
import java.applet.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class InputFourt extends Applet {
    String cgiDir = "/cgi-bin/FT/"; // Directory of the executable program
//    String cgiDir = "/cgi-bin/pasquier-exec/"; // Directory of the executable program
    String initProg = "initFourt";
    String showProg = "showFourt";
    String URLOfExecutable = "o2.db.uoa.gr";

    // Components of the applet
    // TextAreas (names of TextArea begins with 'ta')
    TextArea taSequence;
    TextArea taErrors;

    // TextField (names of TextField begins with 'tf')
    TextField tfStartResidue;
    TextField tfEndResidue;
    TextField tfPower2;
    TextField tfChainID; // used only in PDB format
    TextField tfWeight[] = new TextField[25];
    TextField tfMinIntensity;
    TextField tfMinPeriod;
    TextField tfMaxPeriod;

    // Buttons (names of Buttons begins with 'bu')
    Button buRun;
    Button buCheck;
    Button buClear;
    Button buRunMostFrequent;// 5 most frequent residues
    Button buRunCharged;    // Charged
    Button buRunChargedPos; // Charged positive
    Button buRunChargedNeg; // Charged negative
    Button buRunAromatic;   // Aromatics
    Button buRunAliphatic;  // Aliphatics
    Button buRunBSheet;     // B-Sheet formers
    Button buRunBTurn;      // B-Turn formers
    Button buRunPolar;      // Polars
    Button buRunAHelix;     // A-Helix formers
    Button buRunHydrophob;  // Hydrophobic

    // Checkboxes (names of Checkbox begins with 'cb')
    Checkbox cbStatsInPercent;
    Checkbox cbResultHTML;
    Checkbox cbResultText;
    Checkbox cbChangeToX;
    Checkbox cbIgnoreGap;
    Checkbox cb3LetterCode;
    Checkbox cbFormatNone;
    Checkbox cbFormatFasta;
    Checkbox cbFormatSwissprot;
    Checkbox cbFormatPDB;

    // Labels (names of Label begins with 'lb')
    Label lbStat[] = new Label[3];
    Label lbWeight[] = new Label[25];
    Label lbSeqLength;
    Label lbPower2;
    Label lbEndResidue;
    Label lbMaxPeriod;

    AppletContext appletContext;
    String AALetters    = "ACDEFGHIKLMNPQRSTVWYatcgX";
    String AACharged    = "HRKDE";
    String AAChargedPos = "HRK";
    String AAChargedNeg = "DE";
    String AAAromatic   = "HFWY";
    String AAAliphatic  = "VLIA";
    String AABSheet     = "VLIFWYTCQM";
    String AABTurn      = "GPDNSCKWYQTRE";
    String AAPolar      = "DEHKRSTNQ";
    String AAAHelix     = "AVLIFWDEQMHK";
    String AAHydrophob  = "CVILMFYWAP";

    String strSequence;
    int    valIndex1;
    int    valIndex2;
    int    valPower2;
    float  valMinIntensity;
    float  valMinPeriod;
    float  valMaxPeriod;
    float  valAAWeight[] = new float[25];
    boolean statsInPercent = false;
    int    seqLength;
    String seqTitle;
    String seqText;

    int  statAA[] = new int[25];

    GridBagLayout framegridbag;
    GridBagConstraints c;
    FramedArea pStats;

    boolean firstDisplay = true;
    boolean errorDetected;

    public void init() {
        displayDialog();
        appletContext = getAppletContext();
        validate();
    }
    public void start() {
        if ( firstDisplay ) {
            clearForm();
            InitForm();
            firstDisplay = false;
        }
        PrintLabelWeight();
    }

    protected void displayDialog() {
        GridBagLayout gridbag = new GridBagLayout();
        c = new GridBagConstraints();
          
        setFont(new Font("TimesRoman", Font.PLAIN, 12));
        setLayout(gridbag);
        c.anchor = GridBagConstraints.NORTHWEST;

        Label l;

        // Framed Area for Sequence specification
        framegridbag = new GridBagLayout();
        FramedArea pSeq = new FramedArea();
        pSeq.setLayout(framegridbag);
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        c.fill = GridBagConstraints.HORIZONTAL;
        addFramedArea(this, pSeq, gridbag, c);

        // Title for the sequence Input
        Label title = createNewTitle("Sequence Specification");
        c.gridwidth = 4;
        c.fill = GridBagConstraints.NONE;
        addComponent(pSeq, title, framegridbag, c);

        // Label for the sequence length
        lbSeqLength = new Label("Sequence length: ____");
        lbSeqLength.setFont(new Font("Times", Font.PLAIN, 14));
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        addComponent(pSeq, lbSeqLength, framegridbag, c);

        // Text Area for sequence input
        taSequence = new TextArea(8,70);
        taSequence.setFont(new Font("Courier", Font.BOLD, 12));
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 2;
        c.gridwidth = 7;
        addComponent(pSeq, taSequence, framegridbag, c);

        // Framed Area for format
        Panel pFormat = new FramedArea();
        pFormat.setLayout(framegridbag);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.gridheight = 1;
        addFramedArea(pSeq, pFormat, framegridbag, c);

        // title for the 'format' panel
        title = createNewTitle("Format");
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        addComponent(pFormat, title, framegridbag, c);

        CheckboxGroup cbg = new CheckboxGroup();
        cbFormatNone = new Checkbox("None", cbg, true);
        cbFormatFasta = new Checkbox("FASTA", cbg, false);
        cbFormatSwissprot = new Checkbox("SwissProt", cbg, false);
        cbFormatPDB = new Checkbox("PDB", cbg, false);
        addComponent(pFormat, cbFormatNone, framegridbag, c);
        addComponent(pFormat, cbFormatFasta, framegridbag, c);
        addComponent(pFormat, cbFormatSwissprot, framegridbag, c);
        addComponent(pFormat, cbFormatPDB, framegridbag, c);

        l = new Label("ChainID");
        c.gridwidth = 1;
        addComponent(pFormat, l, framegridbag, c);
        tfChainID = new TextField(2);
        tfChainID.setFont(new Font("Courier", Font.BOLD, 12));
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        addComponent(pFormat, tfChainID, framegridbag, c);

        cbIgnoreGap = new Checkbox("Ignore gaps");
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        addComponent(pSeq, cbIgnoreGap, framegridbag, c);

        // ask for the number that determine the sequence we are interested in
        c.fill = GridBagConstraints.NONE;
        c.gridheight = 1;
        c.gridwidth = 1;
        l = new Label("Start residue ");
        addComponent(pSeq, l, framegridbag,c);
        tfStartResidue = new TextField(5);
        tfStartResidue.setFont(new Font("Courier", Font.BOLD, 12));
        addComponent(pSeq, tfStartResidue, framegridbag, c);

        lbEndResidue = new Label("  End residue (#=last) ");
        c.gridwidth = 1;
        addComponent(pSeq, lbEndResidue, framegridbag, c);
        tfEndResidue = new TextField(5);
        tfEndResidue.setFont(new Font("Courier", Font.BOLD, 12));
        addComponent(pSeq, tfEndResidue, framegridbag, c);

        // ask for the power of 2
        lbPower2 = new Label("  embedding size (#=smallest) ");
        c.gridwidth = 1;
        addComponent(pSeq, lbPower2, framegridbag, c);
        tfPower2 = new TextField(5);
        tfPower2.setFont(new Font("Courier", Font.BOLD, 12));
        c.gridwidth = 1;
        addComponent(pSeq, tfPower2, framegridbag, c);
        l = new Label(" ");
        addComponent(pSeq, l, framegridbag, c);

        cbChangeToX = new Checkbox("Accept Unk.");
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        addComponent(pSeq, cbChangeToX, framegridbag, c);

        // Framed Area for the statistics
        pStats = new FramedArea();
        pStats.setLayout(framegridbag);
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        pStats.setFont(new Font("Courier", Font.BOLD, 12));
        addFramedArea(this, pStats, gridbag, c);

        title = createNewTitle("Statistics");
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        addComponent(pStats, title, framegridbag, c);

        for (int i = 0 ; i < 3 ; i++) {
            lbStat[i] = new Label("__________________________________________________________________________");
            addComponent(pStats, lbStat[i], framegridbag, c);
        }

        Panel pCmd = new FramedArea();
        pCmd.setLayout(framegridbag);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        addComponent(this, pCmd, gridbag, c);

        cbStatsInPercent = new Checkbox("Stats in %");
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        c.fill = GridBagConstraints.NONE;
        addComponent(pCmd, cbStatsInPercent, framegridbag, c);

        cb3LetterCode = new Checkbox("3-letter");
        addComponent(pCmd, cb3LetterCode, framegridbag, c);

        cbg = new CheckboxGroup();
        cbResultHTML = new Checkbox("HTML page", cbg, true);
        cbResultText = new Checkbox("Plain text", cbg, false);
        addComponent(pCmd, cbResultHTML, framegridbag, c);
        addComponent(pCmd, cbResultText, framegridbag, c);

        // Framed Area for the shortcut buttons
        Panel pShortButtons = new FramedArea();
        pShortButtons.setLayout(framegridbag);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        addFramedArea(this, pShortButtons, gridbag, c);

        buRunMostFrequent = new Button("Most frequent");
//        buRunMostFrequent.setFont(new Font("TimesRoman", Font.BOLD, 18));
        buRunCharged = new Button("Charged");
        buRunChargedPos = new Button("Charged+");
        buRunChargedNeg = new Button("Charged-");
        buRunAromatic = new Button("Aromatic");
        buRunAliphatic = new Button("Aliphatic");
        buRunBSheet = new Button("B-sheet");
        buRunBTurn = new Button("B-turn");
        buRunPolar = new Button("Polar");
        buRunAHelix = new Button("a-helix");
        buRunHydrophob = new Button("Hydrophobic");
//        buRunCharged.setFont(new Font("TimesRoman", Font.BOLD, 18));
        c.gridwidth = 1;
//        c.fill = GridBagConstraints.NONE;
//        title = createNewTitle("Submit for: ");
//        addComponent(pShortButtons, title, framegridbag, c);
        addComponent(pShortButtons, buRunMostFrequent, framegridbag, c);
        addComponent(pShortButtons, buRunAHelix, framegridbag, c);
        addComponent(pShortButtons, buRunBSheet, framegridbag, c);
        addComponent(pShortButtons, buRunBTurn, framegridbag, c);
        addComponent(pShortButtons, buRunHydrophob, framegridbag, c);
        addComponent(pShortButtons, buRunPolar, framegridbag, c);
        addComponent(pShortButtons, buRunCharged, framegridbag, c);
        addComponent(pShortButtons, buRunChargedPos, framegridbag, c);
        addComponent(pShortButtons, buRunChargedNeg, framegridbag, c);
        addComponent(pShortButtons, buRunAromatic, framegridbag, c);
        addComponent(pShortButtons, buRunAliphatic, framegridbag, c);

        // Framed Area for the Weight
        Panel pWeight = new FramedArea();
        pWeight.setLayout(framegridbag);

        c.gridheight = 2;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        addFramedArea(this, pWeight, gridbag, c);

        title = createNewTitle("Selection of residues");
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        c.fill = GridBagConstraints.NONE;
        addComponent(pWeight, title, framegridbag, c);

        // specification of one problem
        for ( int i = 0 ; i < 5 ; i++ ) {
            c.gridwidth = 1;
            for (int j = 0 ; j < 5 ; j++) {
                String sLab;
                if ( j==0 ) sLab = "--- "; else sLab = "  --- ";
                lbWeight[j*5+i] = new Label(sLab);
                addComponent(pWeight, lbWeight[j*5+i], framegridbag, c);
                tfWeight[j*5+i] = new TextField(4);
                tfWeight[j*5+i].setFont(new Font("Courier", Font.BOLD, 12));
                if ( j == 4 ) {
                        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
                }
                addComponent(pWeight, tfWeight[j*5+i], framegridbag, c);
            }
        }

        // Framed Area for the Output Formatting

        FramedArea pOutput = new FramedArea();
        pOutput.setLayout(framegridbag);
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridheight = 1;
        addFramedArea(this, pOutput, gridbag, c);
        c.gridheight = 1;

        title = createNewTitle("Filter");
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        c.fill = GridBagConstraints.NONE;
        addComponent(pOutput, title, framegridbag, c);

        l = new Label("Cutoff ");
        c.gridwidth = 1;
        addComponent(pOutput, l, framegridbag, c);
        tfMinIntensity = new TextField(4);
        tfMinIntensity.setFont(new Font("Courier", Font.PLAIN, 12));

        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        c.fill = GridBagConstraints.NONE;
        addComponent(pOutput, tfMinIntensity, framegridbag, c);
        l = new Label("min Period ");
        c.gridwidth = 1;
        addComponent(pOutput, l, framegridbag, c);
        tfMinPeriod = new TextField(4);
        tfMinPeriod.setFont(new Font("Courier", Font.BOLD, 12));
        c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        addComponent(pOutput, tfMinPeriod, framegridbag, c);
        lbMaxPeriod = new Label("max Period (#=----) ");
        c.gridwidth = 1;
        addComponent(pOutput, lbMaxPeriod, framegridbag, c);
        tfMaxPeriod = new TextField(4);
        tfMaxPeriod.setFont(new Font("Courier", Font.BOLD, 12));
        c.gridwidth = 1;
        addComponent(pOutput, tfMaxPeriod, gridbag, c);

        // Framed Area for the buttons
        Panel pButtons = new FramedArea();
        pButtons.setLayout(framegridbag);

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        addFramedArea(this, pButtons, gridbag, c);

        buRun = new Button("Submit");
        buRun.setFont(new Font("TimesRoman", Font.BOLD, 18));
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        addComponent(pButtons, buRun, framegridbag, c);


        buCheck = new Button("Check");
        buCheck.setFont(new Font("TimesRoman", Font.BOLD, 18));
        addComponent(pButtons, buCheck, framegridbag, c);
        buClear = new Button("Reset");
        buClear.setFont(new Font("TimesRoman", Font.BOLD, 18));
        addComponent(pButtons, buClear, framegridbag, c);

        // text area for Errors
        taErrors = new TextArea(5, 80);
        taErrors.setEditable(false);
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        addComponent(this, taErrors, gridbag, c);
    }

    protected Label createNewTitle( String s) {
        Label title = new Label(s);
        title.setFont(new Font("Times", Font.BOLD, 14));
        return title;
    }

    protected void addFramedArea(Panel p,
                                Component comp,
                                GridBagLayout gridbag,
                                GridBagConstraints constr) {
        c.ipadx = 12;
        c.ipady = 12;
        addComponent(p, comp, gridbag, c);
        c.ipadx = 0;
        c.ipady = 0;
    }

    protected void addComponent(Panel p,
                                Component comp,
                                GridBagLayout gridbag,
                                GridBagConstraints constr) {
        gridbag.setConstraints(comp, constr);
        p.add(comp);
    }


    protected void clearForm() {
        lbSeqLength.setText("");
        taSequence.setText("");
        tfStartResidue.setText("1");
        lbEndResidue.setText("  End residue (#=last) ");
        tfEndResidue.setText("#");
        lbPower2.setText("  embedding size (#=smallest) ");
        tfPower2.setText("#");
        for (int i = 0 ; i < 25 ; i++ ) {
            tfWeight[i].setText("");
        }
        for (int i = 0 ; i < 3 ; i++ ) {
            lbStat[i].setText("");
        }
        tfMinIntensity.setText("0");
        tfMinPeriod.setText("3");
        lbMaxPeriod.setText("max Period (#=l/2)");
        tfMaxPeriod.setText("#");
        taErrors.setText("");
        PrintLabelWeight();
    }

    // init the form with the parameters supplied
    // to provide some predefined samples for the user
    protected void InitForm() {
        String rs;

        // read the sequence
        rs = getParameter("seqfile");
        if ( rs != null ) {
            try {
                URL seq = new URL(rs);
                DataInputStream dis = new DataInputStream(seq.openStream());
                String inputLine;
                while ((inputLine = dis.readLine()) != null) {
                    taSequence.appendText(inputLine+'\n');
                }
                dis.close();
            }
            catch (MalformedURLException e) {}
            catch (IOException e) {}
        }

        // read the format
        rs = getParameter("format");
        if ( rs != null ) {
            if ( rs.equalsIgnoreCase("NONE") ) cbFormatNone.setState( true );
            else if ( rs.equalsIgnoreCase("FASTA") ) cbFormatFasta.setState( true );
            else if ( rs.equalsIgnoreCase("SWISSPROT") ) cbFormatSwissprot.setState( true );
            else if ( rs.equalsIgnoreCase("PDB") ) cbFormatPDB.setState( true );
        }

        // read the cutoff
        rs = getParameter("cutoff");
        if ( rs != null ) {
            tfMinIntensity.setText( rs );
        }

        // read the selected residue
        rs = getParameter("selected");
        if ( rs != null ) {
            int indx = AALetters.indexOf(rs);
            if ( indx != -1 ) tfWeight[indx].setText("1");
        }
    }

    public boolean action(Event event, Object arg) {
        Object target = event.target;
        if (target == cbStatsInPercent) {
            PrintStat();
        }
        if (target == cb3LetterCode) {
            PrintStat();
            PrintLabelWeight();
        }
        if (target == buCheck) {
            taErrors.setText("");
            DoStat();
            PrintStat();
        }
        if (target == buClear) {
            clearForm();
        }

        boolean submit = false;  // indicate if we must submit the sequence
        String group = "";       // indicate for which group
        boolean submitGroup = true; //indicate if we want to execute FT for a group
                                    // or sequentially for each residues in a group
        if (target == buRunMostFrequent) {
            DoStat();
            // copy the AA letters and the count in two news arrays
            char letterAA[] = new char[25];
            int  numberAA[] = new int[25];
            for ( int i = 0 ; i < 25 ; i++ ) {
                letterAA[i] = AALetters.charAt(i);
                numberAA[i] = statAA[i];
            }

            // sort the values in statAA and in letterAA
            for ( int i = 0 ; i < 24 ; i++ ) {
                for ( int j = i+1 ; j < 25 ; j++ ) {
                    if ( numberAA[i] < numberAA[j] ) {
                        int temp = numberAA[i];
                        numberAA[i] = numberAA[j];
                        numberAA[j] = temp;
                        char tmpchar = letterAA[i];
                        letterAA[i] = letterAA[j];
                        letterAA[j] = tmpchar;
                    }
                }
            }
            for (int i=0 ; i < 25 ; i++ ) {
                if (numberAA[i] == 0 ) break;
                group+=letterAA[i]; //select the 5 most frequent residue\
            }
            submit = true;
            submitGroup = false;
        }
        if (target == buRunCharged) {
            group = AACharged;
            submit = true;
        }
        if (target == buRunChargedPos) {
            group = AAChargedPos;
            submit = true;
        }
        if (target == buRunChargedNeg) {
            group = AAChargedNeg;
            submit = true;
        }
        if (target == buRunAromatic) {
            group = AAAromatic;
            submit = true;
        }
        if (target == buRunAliphatic) {
            group = AAAliphatic;
            submit = true;
        }
        if (target == buRunBSheet) {
            group = AABSheet;
            submit = true;
        }
        if (target == buRunBTurn) {
            group = AABTurn;
            submit = true;
        }
        if (target == buRunPolar) {
            group = AAPolar;
            submit = true;
        }
        if (target == buRunAHelix) {
            group = AAAHelix;
            submit = true;
        }
        if (target == buRunHydrophob) {
            group = AAHydrophob;
            submit = true;
        }
        if (target == buRun) {
            submit = true;
        }

        if (submit) {
            taErrors.setText("");
            if (submitGroup & group.length() > 0 ) { // we have to select a group of residues
                if (!initParameters(false)) return false;
                for (int i = 0 ; i < 25 ; i++ ) {
                    if ( group.indexOf(AALetters.charAt(i)) == -1 )
                        valAAWeight[i] = 0;
                    else valAAWeight[i] = 1;
                }
            }
            else if (!initParameters(true)) return false;

            URL url;
            URL baseUrl = getDocumentBase();
            String protocol = baseUrl.getProtocol();
            String home = baseUrl.getHost();
            int    port = baseUrl.getPort();
            try {
                url = new URL(protocol, home, port, cgiDir + initProg);
                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                PrintStream outStream = new PrintStream(connection.getOutputStream());

                // send all the parameters to the initprogram
                if (submitGroup) {
                    sendParameters(outStream, true); //send the parameters with the selection
                }
                else { // we write lines (one for each residue)
                    sendParameters(outStream, false); //send the parameters without the selection
                    outStream.println(group.length()); // number of problems
                    for (int i=0 ; i < group.length() ; i++) {
                        outStream.println(group.charAt(i)+" 1 "+group.charAt(i)+" 1");
                    }
                }
                outStream.close();

                // read the name of the data file from the input stream
                DataInputStream inStream = new DataInputStream(connection.getInputStream());
                String dataFile = inStream.readLine();
                inStream.close();

                // show the first page of the result
                url = new URL(protocol, home, port, cgiDir + showProg + "?" + dataFile + "000");
                appletContext.showDocument(url);
            }
            catch ( MalformedURLException e) {
                taErrors.setText("MalformedURLException");
                return false;
            }
            catch ( UnknownServiceException e) {
                taErrors.setText("UnknownServiceException");
                return false;
            }
            catch ( IOException e) {
                taErrors.setText("IOException");
                return false;
            }
            catch ( Exception e) {
                taErrors.setText("ERROR03");
                return false;
            }
        }
        return true;
    }

    // initialise all the parameters and control the data
    //

    protected String getLine(int lineIndex, String seq) {
        String line = "";
        while ((seq.length() > lineIndex) && (seq.charAt(lineIndex) != '\n')) {
            line += seq.charAt(lineIndex);
            lineIndex++;
        }
        return line;
    }
    
    protected char convert3To1(String s) {
        if (s.equals("PHE")) return 'F';
        if (s.equals("LEU")) return 'L';
        if (s.equals("ILE")) return 'I';
        if (s.equals("MET")) return 'M';
        if (s.equals("VAL")) return 'V';
        if (s.equals("SER")) return 'S';
        if (s.equals("PRO")) return 'P';
        if (s.equals("THR")) return 'T';
        if (s.equals("ALA")) return 'A';
        if (s.equals("TYR")) return 'Y';
        if (s.equals("HIS")) return 'H';
        if (s.equals("GLN")) return 'Q';
        if (s.equals("ASN")) return 'N';
        if (s.equals("LYS")) return 'K';
        if (s.equals("ASP")) return 'D';
        if (s.equals("GLU")) return 'E';
        if (s.equals("CYS")) return 'C';
        if (s.equals("TRP")) return 'W';
        if (s.equals("ARG")) return 'R';
        if (s.equals("GLY")) return 'G';
        return 'X'; // Unknown
    }
    
    protected String convert1To3(char c) {
        if (c == 'F') return "PHE";
        if (c == 'L') return "LEU";
        if (c == 'I') return "ILE";
        if (c == 'M') return "MET";
        if (c == 'V') return "VAL";
        if (c == 'S') return "SER";
        if (c == 'P') return "PRO";
        if (c == 'T') return "THR";
        if (c == 'A') return "ALA";
        if (c == 'Y') return "TYR";
        if (c == 'H') return "HIS";
        if (c == 'Q') return "GLN";
        if (c == 'N') return "ASN";
        if (c == 'K') return "LYS";
        if (c == 'D') return "ASP";
        if (c == 'E') return "GLU";
        if (c == 'C') return "CYS";
        if (c == 'W') return "TRP";
        if (c == 'R') return "ARG";
        if (c == 'G') return "GLY";
        if (c == 'a') return "  a";
        if (c == 't') return "  t";
        if (c == 'c') return "  c";
        if (c == 'g') return "  g";
        return "UNK"; // Unknown
    }

    protected boolean getSequence() {
        seqTitle = "";
        seqText = "";
        strSequence = "";
        // read the sequence directly on the textArea or at the URL
        // specified in the text area
        String seq = taSequence.getText();
        if (seq.length() > 7 ) {
            if (seq.substring(0,7).equalsIgnoreCase("HTTP://")) {
                // we have to replace the sequence with the content of the URL
                String line = getLine(0, seq);
                seq = "";
                try {
                    taErrors.appendText("E20: URL of the sequence incorrect.");
                    URL url = new URL(line);
                    URLConnection urlC = url.openConnection();
                    DataInputStream dis = new DataInputStream(urlC.getInputStream());
                    String inputLine;
                    while ((inputLine = dis.readLine()) != null) {
                        seq += inputLine+'\n';
                    }
                    dis.close();
                    taErrors.setText("");
                }
                catch (MalformedURLException e) {
                    errorDetected = true;
                    taErrors.setText("E20: URL of the sequence incorrect.\n");
                }
                catch (IOException e) {
                    errorDetected = true;
                    taErrors.setText("E21: Cannot access the sequence at the specified URL.\n");
                }
            }
        }

        if (cbFormatFasta.getState()) {
            int lineIndex = 0;
            String line;
            do {
                line = getLine(lineIndex, seq);
                lineIndex = lineIndex + line.length()+1;
            } while ((lineIndex < seq.length()) && (line.charAt(0) != '>'));
            if (line.length()>0) seqTitle = line.substring(1);
            if (seq.length() > lineIndex) seqText = seq.substring(lineIndex);
            line += '\n';
        }
        if (cbFormatSwissprot.getState()) {
            int lineIndex = 0;
            String line;
            String title1 = "";
            String title2 = "";
            String title3 = "";
            String cde = "";
            do {
                line = getLine(lineIndex, seq);
                lineIndex = lineIndex + line.length()+1;
                if (line.length() < 2) continue;
                cde = line.substring(0,2);
                if (line.length() > 5) {
                    line += " ";
                    if (cde.equals("ID")) {
                        String subLine = line.substring(5);
                        title1 = subLine.substring(0, subLine.indexOf(' '))+'\n';
                    }
                    if (cde.equals("DE")) title2 += line.substring(5)+'\n';
                    if (cde.equals("OS")) title3 += line.substring(5)+'\n';
                }
            } while ((lineIndex < seq.length()) && (!cde.equals("SQ")));
            line = getLine(lineIndex, seq);
            while (lineIndex < seq.length()) {
                if (line.length()> 1) {
                    if (line.substring(0,2).equals("//")) break;
                    seqText += line;
                }
                lineIndex = lineIndex + line.length()+1;
                line = getLine(lineIndex, seq);
            }
            seqTitle = title1+title2+title3;
        }        
        if (cbFormatPDB.getState() == true) {
            String IDChain = tfChainID.getText();
            if (IDChain.length() == 0) IDChain=" ";
            int lineIndex = 0;
            String line;
            String PDBheader = "";
            String PDBtitle = "";
            String cde = "";
            do {
                line = getLine(lineIndex, seq);
                lineIndex = lineIndex + line.length()+1;
                if (line.length() < 11) continue;
                cde = line.substring(0,6);
                line += " ";
                if (cde.equals("HEADER") && (line.length()>=66)) {
                    PDBheader += line.substring(10, 50) + line.substring(62, 66)+'\n';
                }
                if (cde.equals("TITLE ")) PDBtitle += line.substring(10)+'\n';
                if (cde.equals("SEQRES")) {
                    // if no IDChain is specified and the Sequence contains more
                    // than one chain, then the first one is selected
                    if ((!line.substring(11,12).equals(" ")) && (IDChain.equals(" "))) {
                        IDChain = line.substring(11,12);
                    }

                    if (line.substring(11,12).equals(IDChain)) {
                        for ( int i = 19 ; i < 68 ; i+=4 ) {
                            if (i >= line.length()) break;
                            seqText += convert3To1(line.substring(i, i+3));
                        }
                    }
                }
            } while (lineIndex < seq.length());
            seqTitle = PDBheader+PDBtitle;
        }
        if (cbFormatNone.getState() == true) {
            seqTitle = "";
            seqText = seq;
        }

        seqLength = 0;
        for (int i = 0 ; i < seqText.length() ; i++) {
            char letter = seqText.charAt(i);
            if (letter == ' ' || letter == '\n' || letter == '\t') {
                // don't keep separators
            }
            else if ( Character.isDigit(letter) ) {
                // don't keep numbers
            }
            else if ((letter == '-') && (cbIgnoreGap.getState() == true)) {
                // don't keep gaps in the sequence
            }
            else seqLength++;
        }
        return true;
    }

    protected boolean initParameters( boolean testWeights) {
        errorDetected = false;
        getSequence();

        lbSeqLength.setText("Sequence Length: "+seqLength);
        int pow2 = 2;
        while (pow2 < seqLength) pow2 = pow2 * 2;
        lbPower2.setText("   embedding size (#="+pow2+") ");
        lbEndResidue.setText("   End residue (#="+seqLength+") ");
        // control that each caracter in the sequence is the letter of an Amino-Acid
        int nbErr = 0; // count the errors and stop the check whend more than 10 errors are detected
        for (int i = 0 ; i < seqText.length() ; i++) {
            boolean error = true;
            boolean keepChar = true;
            char letter = seqText.charAt(i);
            for (int j = 0 ; j < AALetters.length() ; j++) {
                if (letter == ' ' || letter == '\n' || letter == '\t') { // don't keep separators
                    error = false;
                    keepChar = false;
                    break;
                }
                if ( Character.isDigit(letter) ) {      // don't keep numbers
                    error = false;
                    keepChar = false;
                    break;
                }
                if (letter == AALetters.charAt(j)) {
                    error = false;
                    break;
                }
                if ( (cbChangeToX.getState() == true) && (Character.isLetter(letter))) {
                    error = false;
                    break;
                }
                if (letter == '-') {
                    error = false;
                    if (cbIgnoreGap.getState() == true) keepChar = false;
                    break;
                }
            }
            if (keepChar == true ) {
                strSequence = strSequence + letter;
            }
            if (error == true) {
                taErrors.appendText("E01: The character '"+letter+"' is not allowed in the sequence.\n");
                errorDetected = true;
                nbErr += 1;
                if ( nbErr > 10 ) {
                    taErrors.appendText("E01: More than 10 'wrong' characters in the sequence. Stop parsing.\n");
                    break;
                }
            }
        }

        String strIndex1   = tfStartResidue.getText().trim();
        String strIndex2   = tfEndResidue.getText().trim();
        String strPower2   = tfPower2.getText().trim();
        String strMinIntensity = tfMinIntensity.getText().trim();
        String strMinPeriod    = tfMinPeriod.getText().trim();
        String strMaxPeriod    = tfMaxPeriod.getText().trim();
        String strAAWeight[] = new String[25];
        for ( int i = 0 ; i < 25 ; i++ ) {
            strAAWeight[i] = tfWeight[i].getText().trim();
            if (strAAWeight[i].length() == 0) strAAWeight[i] = "0";
        }
        if ( strIndex1.length() == 0 ) strIndex1 = "0";
        if ( strIndex2.length() == 0 ) strIndex2 = "0";
        if ( strPower2.length() == 0 ) strPower2 = "0";
        if ( strMinIntensity.length() == 0 ) strMinIntensity = "0";
        if ( strMinPeriod.length() == 0 ) strMinPeriod = "0";
        if ( strMaxPeriod.length() == 0 ) strMaxPeriod = "0";

        try {
            valIndex1       = Integer.parseInt(strIndex1);
        }
        catch (NumberFormatException e) {
            taErrors.appendText("E02: 'Start residue' must be an integer.\n");
            errorDetected = true;
        }

        try {
            if (strIndex2.equals("#")) valIndex2 = seqLength;
            else valIndex2       = Integer.parseInt(strIndex2);
        }
        catch (NumberFormatException e) {
            taErrors.appendText("E03: 'End residue' must be an integer or '#'.\n");
            errorDetected = true;
        }
        try {
            if (strPower2.equals("#")) valPower2 = pow2;
            else valPower2       = Integer.parseInt(strPower2);
        }
        catch (NumberFormatException e) {
            taErrors.appendText("E04: 'embedding size' must be an integer or '#'.\n");
            errorDetected = true;
        }
        try {
            valMinIntensity = Float.valueOf(strMinIntensity).floatValue();
        }
        catch (NumberFormatException e) {
            taErrors.appendText("E05: 'Cutoff' must be a float.\n");
            errorDetected = true;
        }
        try {
            valMinPeriod    = Float.valueOf(strMinPeriod).floatValue();
        }
        catch (NumberFormatException e) {
            taErrors.appendText("E06: 'Minimum period' must be a float.\n");
            errorDetected = true;
        }
        try {
            if (strMaxPeriod.equals("#")) valMaxPeriod = Math.round((valIndex2-valIndex1)/2);
            else valMaxPeriod    = Float.valueOf(strMaxPeriod).floatValue();
        }
        catch (NumberFormatException e) {
            taErrors.appendText("E07: 'Maximum period' must be a float or '#'.\n");
            errorDetected = true;
        }
        lbMaxPeriod.setText("max Period (#="+Math.round((valIndex2-valIndex1)/2)+")");

        if (testWeights) {
            for ( int i = 0 ; i < 25 ; i++ ) {
                try {
                    valAAWeight[i] = Float.valueOf(strAAWeight[i]).floatValue();
                }
                catch (NumberFormatException e) {
                    taErrors.appendText("E08: the weight specified for the '"+AALetters.charAt(i)+" residue' must be a float.\n");
                    errorDetected = true;
                }
            }
        }

        // additional tests
        int seqLength = strSequence.length();
        if (seqLength > 4096) {
            taErrors.appendText("E09: the length of the sequence must not exceed 4096 characters.\n");
            errorDetected = true;
        }
        if (seqLength < 1) {
            taErrors.appendText("E10: a sequence must be specified.\n");
            errorDetected = true;
        }
        if (valIndex1 < 1) {
            taErrors.appendText("E11: 'Start residue' cannot be less than '1'.\n");
            errorDetected = true;
        }
        if (valIndex2 > seqLength ) {
            taErrors.appendText("E12: 'End residue' cannot be greater than the length of the sequence.\n");
            errorDetected = true;
        }
        if (valIndex1 > valIndex2 ) {
            taErrors.appendText("E13: 'End residue' must be greater than 'Start residue'.\n");
            errorDetected = true;
        }
        if (valPower2 < valIndex2 - valIndex1 + 1) {
            taErrors.appendText("E14: 'embedding size' cannot be less than the length of the selected part of the sequence.\n");
            errorDetected = true;
        }
        if (valPower2 > 4096) {
            taErrors.appendText("E15: the maximum value for the 'embedding size' is 4096.\n");
            errorDetected = true;
        }
        int p2 = 2;
        while ((p2 < valPower2) && (p2 < 4096)) p2 = p2*2;
        if (p2 != valPower2) {
            taErrors.appendText("E16: 'embedding size' must be a power of 2.\n");
            errorDetected = true;
        }

        if (valMinPeriod < 2) {
            taErrors.appendText("E17: 'Minimum period' must be greater than '1'.\n");
            errorDetected = true;
        }
        if (valMaxPeriod > valIndex2 - valIndex1 + 1) {
            taErrors.appendText("E18: 'Maximum period' cannot be greater than the length of the selected part of the sequence.\n");
            errorDetected = true;
        }
        if (valMaxPeriod <= valMinPeriod) {
            taErrors.appendText("E19: 'Maximum period' must be greater than the 'Minimum period'.\n");
            errorDetected = true;
        }

        if (errorDetected)
            return false;
        else return true;
    }

    protected boolean sendParameters( PrintStream out, boolean sendSelection ) {
        String userInfo = "";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            userInfo += localHost.getHostName();
            userInfo += '(' + localHost.getHostAddress() + ") ";
        }
        catch (UnknownHostException e) {
            userInfo += "Unknown ";
        }
        userInfo+= "using "+System.getProperty("os.name")+"/"+System.getProperty("os.arch")+" ";

        Date date = new Date();
        userInfo += "logged in on " + date.toString() + ".\n";
        userInfo = '*' + userInfo; // '*' is checked by initFourt to confirm the validity of data
        out.print(userInfo);
        // send the title
        seqTitle = seqTitle.trim();
        out.print(seqTitle);
        out.println('\0'); // end of the title

        // send the sequence
        for ( int i = 0 ; i < seqLength ; i++ ) {
            char c = strSequence.charAt(i);
            if (AALetters.indexOf((int)c) == -1)
                    out.print("X");
                else out.print(c);
        }
        out.println("");
        out.print(valIndex1);
        out.print(' ');
        out.print(valIndex2);
        out.print(' ');
        out.println(valPower2);
        out.print(valMinIntensity);
        out.print(' ');
        out.print(valMinPeriod);
        out.print(' ');
        out.println(valMaxPeriod);
        // send the display mode of the result
        if (cbResultHTML.getState() == true) 
             out.println('H'); // for HTML format
        else out.println('T'); // for plain text format
        // send the code to use to display the residues
        if (cb3LetterCode.getState() == true) 
             out.println('3'); // 3-letter code
        else out.println('1'); // 1-letter code

        if (sendSelection) {
            // count  and send the number of selected Amino_Acids
            int nbAA=0; // number of selected Amino-Acids
            for (int i = 0 ; i < 25 ; i++ ) {
                if ( valAAWeight[i] != 0 ) {
                    nbAA++;
                }
            }
            out.println(1); // one problem
            out.print("oneProblem ");
            out.print(nbAA);
            out.print(' ');

            // send the selected Amino-Acids and their weight
            for (int i = 0 ; i < 25 ; i++ ) {
                if ( valAAWeight[i] != 0 ) {
                    out.print(AALetters.charAt(i)+" ");
                    out.print(valAAWeight[i]+" ");
                }
            }
            out.println();
        }
        return true;
    }

    protected boolean DoStat() {
        initParameters(true);
        String seq = seqText;
        seqLength = 0;
        for ( int j = 0 ; j < 25 ; j++ ) statAA[j] = 0;
        for (int i = 0 ; i < seq.length() ; i++) {
            char letter = seq.charAt(i);
            if (letter != ' ' && letter != '\n' && letter != '\t') { // don't keep separators
                seqLength++;
            }
            boolean letterFound = false;
            for ( int j = 0 ; j < 25 ; j++ ) {
                if ( AALetters.charAt(j) == letter ) {
                    statAA[j]++;
                    letterFound = true;
                    break;
                }
            }
            if (letterFound == false) {
                if (Character.isLetter(letter) && (cbChangeToX.getState() == true)) {
                    statAA[24]++;
                }
                if ((letter == '-') && (cbIgnoreGap.getState() == false)) {
                    statAA[24]++;
                }
            }
        }
        return true;
    }

    protected void PrintLabelWeight() {
        for ( int i = 0 ; i < 25 ; i++ ) {
            String code = "";
            if (!cb3LetterCode.getState()) code = "  "+AALetters.charAt(i);
            else code = convert1To3(AALetters.charAt(i));
            if (i < 5) {
                lbWeight[i].setText(code);
            }
            else {
                lbWeight[i].setText("  "+code);
            }
        }
    }

    protected boolean PrintStat() {
        // copy the AA letters and the count in two news arrays
        char letterAA[] = new char[25];
        int  numberAA[] = new int[25];
        for ( int i = 0 ; i < 25 ; i++ ) {
            letterAA[i] = AALetters.charAt(i);
            numberAA[i] = statAA[i];
        }

        // sort the values in statAA and in letterAA
        for ( int i = 0 ; i < 24 ; i++ ) {
            for ( int j = i+1 ; j < 25 ; j++ ) {
                if ( numberAA[i] < numberAA[j] ) {
                    int temp = numberAA[i];
                    numberAA[i] = numberAA[j];
                    numberAA[j] = temp;
                    char tmpchar = letterAA[i];
                    letterAA[i] = letterAA[j];
                    letterAA[j] = tmpchar;
                }
            }
        }

        boolean statsInPercent = cbStatsInPercent.getState();
        boolean use3LetterCode = cb3LetterCode.getState();
        for (int line=0 ; line < 3 ; line++) {
            String lineContent = "";
            int nbLabelPerLine = 7;
            for (int nbCol=0 ; nbCol < nbLabelPerLine ; nbCol++ ) {
                int index = line*nbLabelPerLine + nbCol;
                if ( numberAA[index] > 0 ) {
                    String code; // will contains the code of the residue
                    if (!use3LetterCode) code = " "+letterAA[index]+" ";
                    else code = convert1To3(letterAA[index]);
                    if (statsInPercent == true) {
                        float countAA = numberAA[index];
                        float totalAA = seqLength;
                        float percent = countAA/totalAA*100;
                        int percentInt = (int)percent;
                        int firstDigit = (int)((percent - (float)percentInt)*10);
                        String extraSpace;
                        if (percent < 10) extraSpace = "  ";
                        else extraSpace = " ";
                        lineContent+=code+extraSpace+percentInt+"."+firstDigit+"%";
                    }
                    else {
                        String number = " "+numberAA[index];
                        for (int i = number.length() ; i < 5 ; i++ ) number = " "+number;
                        lineContent+=code+number+" ";
                    }
                    lineContent+="  ";
                }
            }
        lbStat[line].setText(lineContent);
        }
        return true;
    }
}

/* This class exists solely to put a frame around the coordinate area. */
class FramedArea extends Panel {
    public FramedArea() {
        super();
        validate();

    }

    public void paint(Graphics g) {
        Dimension d = size();
        Color bg = getBackground();
 
        g.setColor(bg);
        g.draw3DRect(0, 0, d.width - 1, d.height - 1, true);
        g.draw3DRect(3, 3, d.width - 7, d.height - 7, false);
    }
}
