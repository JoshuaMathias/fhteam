import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;
import weka.core.matrix.DoubleVector;

public class Robobit {
	private int obitCount;
	private int max;
	private ArrayList<String> obitBodies;
	private ArrayList<Obit> obits;
	private ArrayList<File> truths;
	private ArrayList<File> obitFiles;
	private Writer writer;
	private String outFileStr;
	private int missingTruths;
	private String truthFilesPath;
	private Map<String, Map<Double, Double>> probabilityMap;
	private int normalCount;
	private int multipleCount;
	private boolean isMultDec;
	private int numMult;

	public Robobit() {
		super();
		this.obitCount = 0;
		this.obitBodies = new ArrayList<String>();
		this.truths = new ArrayList<File>();
		this.obitFiles = new ArrayList<File>();
		this.obits = new ArrayList<Obit>();
		this.probabilityMap = new HashMap<String, Map<Double, Double>>();
		this.normalCount = 0;
		this.multipleCount = 0;
		this.isMultDec=false;
		this.numMult=0;
	}

	// Get text from file
	public static String readStream(InputStream is) {
		StringBuilder sb = new StringBuilder(512);
		try {
			Reader r = new InputStreamReader(is, "UTF-8");
			int c = 0;
			while ((c = r.read()) != -1) {
				sb.append((char) c);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}

	// Get individual words from text
	public String[] getWords(String obitText) {
		String[] words = obitText
				.split("[[ ]*|[,]*|[\\.]*|[:]*|[/]*|[!]*|[?]*|[+]*]+");
		return words;
	}

	public void getFileObits(File file) {
		obits.clear();
		parseTruthFile(file);
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("File " + file.getName() + " not found");
		}
		String fileString = readStream(inStream);
		// System.out.println(fileString.length());
		Matcher matchObit = Pattern.compile("<UNQ>.*(\\n.*?)*</SBODY>")
				.matcher(fileString);
		boolean foundWord = false;
		while (matchObit.find()) {
			// System.out.println("Found obit");
			foundWord = true;
			// Obit newObit = new Obit();
			Obit obit = null;
			if (obits.size() > 0) {
				Matcher matchID = Pattern.compile("\\d.*.*_TXT").matcher(
						matchObit.group());
				if (matchID.find()) {
					// System.out.println(matchID.group());
					obit = findObit(matchID.group().substring(0,
							matchID.group().length() - 4));
					// newObit.setId(matchID.group().substring(0,
					// matchID.group().length() - 4));
				}
				if (obit != null) {
					Matcher matchBody=null;
					if (isMultDec) {
						matchBody = Pattern.compile(
								"<UNQ>.*?(\\n.*?)*?</SBODY>").matcher(
								matchObit.group());
					} else {
						matchBody = Pattern.compile(
							"<SBODY>.*?(\\n.*?)*?</SBODY>").matcher(
							matchObit.group());
					}

					if (matchBody.find()) {
						Matcher matchPerson = Pattern
								.compile(
										"<ENAMEX TYPE=\"PERSON\">.*?(\\n.*?)*?</ENAMEX>")
								.matcher(matchObit.group());
						while (matchPerson.find()) {
							String personFound = matchPerson.group();
							obit.addPerson(
									personFound.substring(22,
											personFound.length() - 9), false);
						}
					}
					
					obit.setTaggedBody(matchBody.group());
					// obit.setBody(obit.getTaggedBody()
					// .replaceAll("<[^(>)]*>|\\$|\\%", "")
					// .replaceAll("&#034;", "\\\"")
					// .replaceAll("&#039;", "\\\'")
					// .replaceAll("&#151;", "-").replaceAll("&#.*;", ""));
					obit.setBody(obit
							.getTaggedBody()
							.replaceAll(
									"<[^(>)]*>|\\$|\\%|&#.*;|-|\'|\"|\\[|\\]|\\(|\\)",
									"").toLowerCase());
					// obits.add(newObit);
				} else {
					// System.out.println("obit is null");
				}
			}
		}
	}

	// Make Obit objects with their id and body.
	public void getObitsInfo() {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFileStr), "utf-8"));
			this.writer = writer;
			this.writer.write("Multiple deceased obituaries\n\n");
			for (File file : obitFiles) {
				obits.clear();
				parseTruthFile(file);
				FileInputStream inStream = null;
				try {
					inStream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					System.out.println("File " + file.getName() + " not found");
				}
				String fileString = readStream(inStream);
				// System.out.println(fileString.length());
				Matcher matchObit = Pattern.compile("<UNQ>.*(\\n.*?)*</SBODY>")
						.matcher(fileString);
				boolean foundWord = false;
				while (matchObit.find()) {
					// System.out.println("Found obit");
					foundWord = true;
					// Obit newObit = new Obit();
					Obit obit = null;
					if (obits.size() > 0) {
						Matcher matchID = Pattern.compile("\\d.*.*_TXT")
								.matcher(matchObit.group());
						if (matchID.find()) {
							// System.out.println(matchID.group());
							obit = findObit(matchID.group().substring(0,
									matchID.group().length() - 4));
							// newObit.setId(matchID.group().substring(0,
							// matchID.group().length() - 4));
						}
						if (obit != null) {
							Matcher matchBody = Pattern.compile(
									"<SEC>.*?(\\n.*?)*?</SBODY>").matcher(
									matchObit.group());

							if (matchBody.find()) {

								// System.out.println("Found obit");
								if (obit.isMultDec()) {
									// System.out.println("Is multdec");
									obitCount++;
									ArrayList<String> deceased = obit
											.getDeceased();
									String decString = "Deceased:\n";
									for (String name : deceased) {
										decString += name + "\n";
									}
									try {
										writer.write(obit.getId() + "\n"
												+ decString + matchBody.group()
												+ "\n\n");
									} catch (IOException e) {
										e.printStackTrace();
										System.out
												.println("Could not write to file");
									}
								}
								// newObit.setBody(matchBody.group());
								// System.out.println("body: "+newObit.getBody()+"end body"
								// );
							}
							// obits.add(newObit);
						} else {
							// System.out.println("obit is null");
						}
					}
				}
				// if (!foundWord) {
				// System.out.println("No word found");
				// }
			}
			writer.write("\n\nNumber of obituaries with multiple deceased: "
					+ obitCount);
		} catch (IOException e) {

		}
	}

	private ArrayList<String> getBodyTexts(String fileText) {
		ArrayList<String> obitBodies = new ArrayList<String>();
		Matcher m = Pattern.compile("<SBODY>.*</SBODY>").matcher(fileText);
		int temp = 0;
		while (m.find()) {
			temp++;
			obitBodies.add(m.group());
		}
		// obitCount+=obitBodies.size();
		if (temp > max) {
			max = temp;
		}
		// System.out.println("Number of obituaries in file: "+obitBodies.size());
		/*
		 * if (obitBodies.size()>0) { System.out.println("First obituary:");
		 * System.out.println(obitBodies.get(0)); }
		 */
		return obitBodies;
	}

	// Return the Obit with the given id
	private Obit findObit(String id) {
		for (Obit obit : obits) {
			if (obit.getId().equals(id)) {
				return obit;
			}
		}
		return null;
	}

	// Parse the truths for one file
	private void parseTruthFile(File file) {
		if (file.getName().contains(".autoEnamex.autoRelex")) {
			file = new File(truthFilesPath
					+ file.getName().substring(0, file.getName().length() - 20)
					+ "truth");
		} else {
			file = new File(file.getAbsolutePath() + ".truth");
		}
		if (file.exists()) {
			Scanner scan = null;
			try {
				scan = new Scanner(file);
			} catch (FileNotFoundException e) {
				System.out.println("File " + file.getName() + " not found");
			}
			if (scan.hasNextLine()) {
				// Skip the truth labels
				scan.nextLine();
			}
			Obit foundObit = null;
			while (scan.hasNextLine()) {
				String truthLine = scan.nextLine();
				// It's only necessary to find the obituary after every +++++
				if (truthLine.equals("+++++")) {
					foundObit = null;
				}
				String[] truthData = truthLine.split("\",\"");
				if (truthData.length > 3) {
					// Remove parenthesis on id from truth file
					// truthData[2] = truthData[2].substring(1,
					// truthData[2].length() - 1);
					if (foundObit == null) {
						foundObit = findObit(truthData[2]);
					}
					if (foundObit != null) {
						// System.out.println("Found obituary: "+truthData[2]);
						foundObit.addTruth(truthData);
					} else {
						Obit newObit = new Obit();
						newObit.setId(truthData[2]);
						newObit.addTruth(truthData);
						obits.add(newObit);

						// System.out.println("Could not find obituary "
						// + truthData[2]);
					}
				}
			}
		} else {
			missingTruths++;
			System.out
					.println("Could not find file: " + file.getAbsolutePath());
		}
	}

	// Add truths to their respective obituaries by IMAGE_ID
	private void parseTruths() {
		for (File file : truths) {
			Scanner scan = null;
			try {
				scan = new Scanner(file);
			} catch (FileNotFoundException e) {
				System.out.println("File " + file.getName() + " not found");
			}
			if (scan.hasNextLine()) {
				// Skip the truth labels
				scan.nextLine();
			}
			Obit foundObit = null;
			while (scan.hasNextLine()) {
				String truthLine = scan.nextLine();
				// It's only necessary to find the obituary after every +++++
				if (truthLine.equals("+++++")) {
					foundObit = null;
				}
				String[] truthData = truthLine.split(",");
				if (truthData.length > 2) {
					// Remove parenthesis on id from truth file
					// truthData[2] = truthData[2].substring(1,
					// truthData[2].length() - 1);
					if (foundObit == null) {
						foundObit = findObit(truthData[2]);
					}
					if (foundObit != null) {
						// System.out.println("Found obituary: "+truthData[2]);
						foundObit.addTruth(truthData);
					} else {
						Obit newObit = new Obit();
						newObit.setId(truthData[2]);
						newObit.addTruth(truthData);
						obits.add(newObit);

						// System.out.println("Could not find obituary "
						// + truthData[2]);
					}
				}
			}
		}
	}

	// If the path is a directory, recursively get a list of all the files.
	private void listFiles(File path) {
		if (path.isDirectory()) {
			for (File currentFile : path.listFiles()) {
				InputStream inStream = null;
				if (currentFile.isFile()) {
					if (currentFile.getName().contains(".truth")) {
						// truths.add(currentFile);
					} else {
						obitFiles.add(currentFile);
					}
				} else if (currentFile.isDirectory()) {
					listFiles(currentFile);
				}
			}
		} else if (obitFiles.size() == 0) {
			obitFiles.add(path);
		}
	}

	// Deprecated. Use listFiles instead.
	@Deprecated
	private void readDirectory(File directoryName) {

		for (File currentFile : directoryName.listFiles()) {
			InputStream inStream = null;
			if (currentFile.isFile()) {
				try {
					inStream = new FileInputStream(currentFile);
					String fileString = readStream(inStream);
					ArrayList<String> obitBodies = getBodyTexts(fileString);
				} catch (FileNotFoundException e1) {
					System.out.println("File " + currentFile.getName()
							+ " not found");
				}
			} else if (currentFile.isDirectory()) {
				readDirectory(currentFile);
			}
		}
	}

	// Deprecated. Only for counting the number of obituaries.
	@Deprecated
	public void parseObits(String obitFileName) {
		File obitFile = new File(obitFileName);
		InputStream inStream = null;
		if (obitFile.exists()) {
			if (obitFile.isDirectory()) {
				System.out.println("Reading directory " + obitFileName);
				readDirectory(obitFile);
			} else if (obitFile.isFile()) {
				System.out.println("Reading file " + obitFileName);
				try {
					inStream = new FileInputStream(obitFile);
					String fileString = readStream(inStream);
					getBodyTexts(fileString);
				} catch (FileNotFoundException e1) {
					System.out.println("Input file not found");
					return;
				}
			}
			System.out.println("Total number of obituaries: " + obitCount);
			System.out.println("Max number of obituaries in a file: " + max);
		} else {
			System.out.println("Input path or file not found");
		}
	}

	// Print a file containing the obituaries that have multiple deceased
	public void getMultDec(String inFileStr, String truthPathStr, String outFileStr, int numMult) {
		System.out.println("findMultDec");
		this.outFileStr = outFileStr;
		this.isMultDec=true;
		this.numMult=numMult;
		this.truthFilesPath=truthPathStr;
		if (this.numMult==0) {
			this.numMult=Integer.MAX_VALUE;
		}
		// try (Writer writer = new BufferedWriter(new OutputStreamWriter(
		// new FileOutputStream(outFileStr), "utf-8"))) {
		// this.writer = writer;
		// this.writer.write("Multiple deceased obituaries\n");
		// } catch (IOException e) {
		// System.out.println("Error writing to file");
		// return;
		// }

		File inFile = new File(inFileStr);
		InputStream inStream = null;
		if (inFile.exists()) {
			System.out.println("Reading directory " + inFileStr);
			// parseFiles(inFile);
			listFiles(inFile);
//			for (Obit obit : obits) {
//				if (obit.isMultDec()) {
//					obitCount++;
//					obitBodies.add(obit.getBody());
//				}
//			}

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFileStr), "utf-8"))) {
//				writer.write("Number of obituaries with multiple deceased: "
//						+ obitCount + "\n");
				this.writer=writer;
				writer.write("Obituaries with multiple deceased:\n\n");
//				for (String obitBody : obitBodies) {
//					writer.write(obitBody);
//				}
				for (File file : obitFiles) {
					if (obitCount<this.numMult) {
						getFileObits(file);
						for (Obit obit : obits) {
							if (obit.isMultDec()) {
								
								obitCount++;
								writer.write(obit.getTaggedBody()+"\n\n");
							}
						}
					}
				}
			} catch (IOException e) {
				System.out.println("Error writing to file");
				return;
			}
//
//			try {
//				writer.write("Number of obituaries with multiple deceased: "
//						+ obitCount + "\n");
//				if (missingTruths > 0) {
//					writer.write("Number of missing truth files: "
//							+ missingTruths + "\n");
//				}
//			} catch (IOException e) {
//			}
			System.out.println("Number of obituaries with multiple deceased: "
					+ obitCount);
		} else {
			System.out.println("Input path or file not found");
		}
	}

	public void generateProbabilityMap(File probabilityFile) {

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(probabilityFile), "utf-8"));

			reader.readLine();
			String line;
			while ((line = reader.readLine()) != null) {
				String[] lineSplit = line.split(",");
				if (lineSplit.length > 1) {
					if (this.probabilityMap.containsKey(lineSplit[0]
							.toLowerCase())) {
						Map<Double, Double> map = this.probabilityMap
								.get(lineSplit[0]);
						if (map != null) {
							if (map.containsKey(Double
									.parseDouble(lineSplit[1]))) {
								double prop = map.get(Double
										.parseDouble(lineSplit[1]));
								double temp = (prop + Double
										.parseDouble(lineSplit[2])) / 2;
								map.remove(Double.parseDouble(lineSplit[1]));
								map.put(Double.parseDouble(lineSplit[1]), temp);
							} else {
								map.put(Double.parseDouble(lineSplit[1]),
										Double.parseDouble(lineSplit[2]));
							}
						}
					} else {
						HashMap<Double, Double> distancePair = new HashMap<Double, Double>();
						distancePair.put(Double.parseDouble(lineSplit[1]),
								Double.parseDouble(lineSplit[2]));
						if (lineSplit[0].toLowerCase().equals("age"))
							System.out.println("Put word: "
									+ lineSplit[0].toLowerCase());
						this.probabilityMap.put(lineSplit[0].toLowerCase(),
								distancePair);
					}
					if (lineSplit[0].equals("age")
							&& lineSplit[1].equals("1.0")) {

						this.probabilityMap.get("age");

					}
					if (lineSplit[0].equals("age")) {
						this.probabilityMap.get("age");
					}
				}
				// if (this.probabilityMap!=null &&
				// this.probabilityMap.get("age")!=null)
				// System.out.println("age size: "+this.probabilityMap.get("age").size());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void guessDecedent(String probabilityPath, String enamexPath,
			String truthPath, String outputFile) {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "utf-8"));
			this.writer = writer;
			truthFilesPath = truthPath;
			File inFilePath = new File(enamexPath);
			if (inFilePath.exists()) {
				File probabilityFile = new File(probabilityPath);
				this.generateProbabilityMap(probabilityFile);
				this.outFileStr = outputFile;
				this.listFiles(inFilePath);
				int correctGuesses = 0;
				int totalGuesses = 0;
				final double MIN_PROB = 0.0000000001;
				for (File file : obitFiles) {
					getFileObits(file);
					for (Obit obit : this.obits) {
						List<String> wordList = obit.getWordListOfBody();
						final int WORD_LIST_DEFAULT = wordList.size() + 100;
						List<String[]> names = obit.getNames();
						String decedentName = "";
						if (obit.getNames().size() > 0) {
							decedentName = obit.getNames().get(0)[0];
						}
						double probabilityThatNameIsDecedent = 0.0;
						for (int i = 0; i < names.size(); i++) {
							String[] name = names.get(i);
							ArrayList<Double> probabilityForName = new ArrayList<Double>();
							for (int j = 0; j < wordList.size(); j++) {
								String word = wordList.get(j).toLowerCase();
								int distance = WORD_LIST_DEFAULT;
								int indexOfName = wordList.indexOf(name[0]);
								if (indexOfName != -1) {
									distance = j - indexOfName;
								}
								if (distance != 0) {
									if (distance != WORD_LIST_DEFAULT) {
										if (this.probabilityMap
												.containsKey(word)) {
											Map<Double, Double> distancePair = this.probabilityMap
													.get(word);
											if (distancePair
													.containsKey((double) distance)) {
												probabilityForName
														.add(this.probabilityMap
																.get(word)
																.get((double) distance));
											} else {
												probabilityForName
														.add(MIN_PROB);
											}
										} else {
											probabilityForName.add(MIN_PROB);
										}
									}
								}
							}

							double probAverage = 0.0;
							for (int probIter = 0; probIter < probabilityForName
									.size(); probIter++) {
								probAverage += probabilityForName.get(probIter);
							}
							probAverage = probAverage
									/ probabilityForName.size();
							if (probAverage > probabilityThatNameIsDecedent) {
								probabilityThatNameIsDecedent = probAverage;
								decedentName = name[0];
							}
						}

						if (obit.isDeceased(decedentName)) {
							correctGuesses++;
							totalGuesses++;
							// this.writer.write("1");
						} else {
							totalGuesses++;
							// this.writer.write("0");
						}
					}
				}
				double accuracy = (double) correctGuesses
						/ (double) totalGuesses;
				this.writer.write("Accuracy: " + accuracy);
			} else {
				System.out.println("Enamex file path doesn't exist");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {

			}
		}
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public void getWordDistance(String enemexPath, String truthPath,
			String outputFile) {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "utf-8"));
			System.out.println("Get word distance");
			truthFilesPath = truthPath;
			File inFilePath = new File(enemexPath);
			if (inFilePath.exists()) {
				this.outFileStr = outputFile;
				this.listFiles(inFilePath);
				this.writer = writer;
				this.writer.write("Word,Distance From Name,Is Name Deceased\n");
				for (File file : obitFiles) {
					getFileObits(file);
					int iterator = 0;
					for (Obit obit : this.obits) {
						iterator++;
						List<String> wordList = obit.getWordListOfBody();
						final int WORD_LIST_DEFAULT = wordList.size() + 100;
						List<String[]> names = obit.getNames();
						for (int i = 0; i < names.size(); i++) {
							String[] name = names.get(i);
							boolean isDeceased = false;
							if (name[1] == "1") {
								isDeceased = true;
							}
							// boolean isDeceased =
							// obit.getDeceased().contains(name);
							for (int j = 0; j < wordList.size(); j++) {
								int distance = WORD_LIST_DEFAULT;
								int indexOfName = wordList.indexOf(name[0]);
								if (indexOfName != -1) {
									distance = j - indexOfName;
								}
								if (distance != 0) {
									if (distance == WORD_LIST_DEFAULT) {
										// this.writer.write(wordList.get(j)
										// + "|infinite|" + isDeceased + "\n");
									} else {
										if (name[1].equals("0")
												|| name[1].equals("1")) {
											String currentWord = wordList
													.get(j);
											if (isNumeric(currentWord)
													&& currentWord.length() == 2) {
												currentWord = "10";

											}
											String printString = currentWord
													+ "," + distance + ","
													+ isDeceased + "\n";

											this.writer.write(printString);
										}
									}
								}
							}
						}
					}
				}
			} else {
				System.out.println("Enamex file path doesn't exist");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {

			}
		}
	}

	public void getWordProbs(String learningPath, String outputPath) {
		DataSource source = null;
		System.out.println("Get word probabilities");
		try {
			source = new DataSource(learningPath);
			try {
				Instances data = source.getDataSet();
				System.out.println("Unique words: "
						+ data.numDistinctValues(data.attribute(0)));

				String[] options = new String[1];
				options[0] = "-D";
				// options[1] = "";
				data.setClassIndex(data.numAttributes() - 1);
				Classifier naiveB = new NaiveBayes();
				naiveB.setOptions(options);
				// System.out.println(data.classAttribute());
				naiveB.buildClassifier(data);
				Collection<Instance> uniqueInstances = new InstanceSet<Instance>();
				Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputPath), "utf-8"));
				// writer.write("Word,Distance,Probability "
				// + data.classAttribute().value(0) + ",Probability "
				// + data.classAttribute().value(1) + "\n");
				writer.write("Word,Distance,Probability "
						+ data.classAttribute().value(1) + "\n");
				for (int i = 0; i < data.numInstances(); i++) {
					double[] probs = naiveB.distributionForInstance(data
							.instance(i));
					if (!uniqueInstances.contains(data.instance(i))) {
						// writer.write(data.instance(i).stringValue(0) + ","
						// + data.instance(i).value(1) + "," + probs[0]
						// + "," + probs[1] + "\n");
						writer.write(data.instance(i).stringValue(0) + ","
								+ data.instance(i).value(1) + "," + probs[1]
								+ "\n");
					}
					uniqueInstances.add(data.instance(i));
				}
				// writer.write(naiveB.toString());

			} catch (Exception e) {
				System.out.println("Could not get data");
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println("Weka could not import file " + learningPath);
			e.printStackTrace();
		}
	}

	// Go through all files and make a list of Obit objects.
	public void parseFiles(File inPath) {
		listFiles(inPath);
		// parseTruths();
		getObitsInfo();
	}

	public int getObitCount() {
		return obitCount;
	}

	public void setObitCount(int obitCount) {
		this.obitCount = obitCount;
	}

}