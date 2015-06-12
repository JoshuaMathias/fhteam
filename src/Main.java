public class Main {

	/**
	 * Give the dictionary file name as the first argument and the word to
	 * correct as the second argument.
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out
					.println("Usage: robobit [command] [arguments]\nEnter obitrobo help for more information.");
		} else if (args[0].equals("-h") || args[0].equals("help")) {
			System.out
					.println("Usage: robobit [command] [arguments]\n"
                            + "Commands:"
                            + "parseobits [input file or dir] [output filename (optional)]\t Parses and stores information from obituaries.\n"
                            + "getobits [input path to obituaries] [input truth files path] [output filename] [number of obituaries to find (default all)]\t Counts and prints to a file obituaries that don't have multiple deceased.\n"
                            + "getmultdec [input path to obituaries] [input truth files path] [output filename] [number of obituaries to find (default all)]\t Counts and prints to a file obituaries with multiple deceased.\n"
                            + "getworddist [input ENEMEX file path] [input truth files path] [output filename]\t Parses the obitaries and prints to a csv file a list of the words with their distances from each name in the obituaries"
                            + "getwordprobs [input learning csv file path] [output file path]\t Creates a probability table for each word in the training data using Weka's Naive Bayes\n"
                            + "guessdecedent [input probability table file (output from getwordprobs)] [input testing ENEMEX file path] [input testing truth file path] [output file path]\t Learns from the probability table and then tests on the testing data. The output file has the results and accuracy");
		} else if (args[0].equals("parseobits")) {
			if (args.length > 1) {
				Robobit robobit = new Robobit();
				robobit.parseObits(args[1]);
			} else {
				System.out
                        .println("Usage: parseobits [input filename] [output filename (optional)]");
			}
		} else if (args[0].equals("getworddist")) {
			if (args.length > 3) {
				System.out.println("Getting word distances...");
				Robobit robobit = new Robobit();
				robobit.getWordDistance(args[1], args[2], args[3]);
			} else {
				System.out.println("Usage: getworddist [input ENEMEX file path] [input truth file path] [output filename]");
			}
		} else if (args[0].equals("getmultdec") || args[0].equals("getobits")) {
			Robobit robobit = new Robobit();
			String inPath="";
			String outFile="";
			String truthPath="";
			int numMult=0;
			try {
				if (args.length > 1) {
					inPath = args[1];
				} else {
					throw new Exception("No input file entered.");
				}
			} catch (Exception e) {
				System.out.println(e);
				return;
			}
			if (args.length > 3) {
				truthPath=args[2];
				outFile = args[3];
			} else {
//				outFile = "multiple_deceased_output.txt";
				System.out.println("Please enter the path to the truth files and output file");
			}
			if (args.length > 4) {
				numMult=Integer.parseInt((args[4]));
			}
			boolean isMult=true;
			if (args[0].equals("getobits")) {
				isMult=false;
			}
			robobit.getMultDec(inPath, truthPath, outFile, numMult, isMult);
		} else if (args[0].equals("getwordprobs")) {
			Robobit robobit = new Robobit();
			String inFile;
			String outFile;
			try {
				if (args.length > 1) {
					inFile = args[1];
				} else {
					throw new Exception("No input file entered.");
				}
			} catch (Exception e) {
				System.out.println(e);
				return;
			}
			if (args.length > 2) {
				outFile = args[2];
			} else {
				outFile = "learnedtable.csv";
			}
			robobit.getWordProbs(inFile, outFile);
		}
        else if (args[0].equals("guessdecedent")) {
            Robobit robobit = new Robobit();
            try {
                robobit.guessDecedent(args[1], args[2], args[3], args[4], false);
            } catch (Exception e) {
                System.out.println("Usage: guessdecedent [input probability table file (output from getwordprobs)] [input testing ENEMEX file path] [input testing truth file path] [output file path]\t Learns from the probability table and then tests on the testing data. The output file has the results and accuracy");
                e.printStackTrace();
            }
        }
        else if (args[0].equals("base")) {
            Robobit robobit = new Robobit();
            try {
                robobit.guessDecedent(args[1], args[2], args[3], args[4], true);
            } catch (Exception e) {
                System.out.println("Usage: base [input probability table file (output from getwordprobs)] [input testing ENEMEX file path] [input testing truth file path] [output file path]\t Learns from the probability table and then tests on the testing data. The output file has the results and accuracy");
                e.printStackTrace();
            }
        }
	}

}
