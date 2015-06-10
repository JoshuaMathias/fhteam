
import java.util.*;

public class Obit {
	private String id;
	private ArrayList<String[]> truths;
	private String body;
	private String taggedBody;
	private List<String[]> names;
	private ArrayList<String> deceased;

	public Obit() {
		id = "";
		truths = new ArrayList<String[]>();
		body = "";
		taggedBody = "";
		names = new ArrayList<String[]>();
		deceased = new ArrayList<String>();
	}
	
	public String formatName(String person) {
	      person = person.toLowerCase();
	            boolean hasComma = false;
	            if (person.contains(",") || person.contains("&#44;")) {
	                hasComma = true;
	            }

	            // String[]
	            // personWords=person.split("[[ ]*|[,]*|[\\.]*|[:]*|[/]*|[!]*|[?]*|[<.*>]*|[+]*]+");
	            person = person.replaceAll("&#034;", "\\\"").replaceAll("&#039;", "\\\'").replaceAll("&#151;", "-").replaceAll("<.*>", "").replaceAll("-", "");
	            String[] personWords = person.split("[\\P{L}]*]+");
	            List<String> personWordsList = new ArrayList<String>();
	            for (String word : personWords) {
	                if (!(word.equals("") || word.equals("jr") || word.equals("sr"))) {
	                    personWordsList.add(word);
	                }
	            }
	            String lastName = "";
	            String givenNames = "";
	            if (personWordsList.size() > 1) {
	                if (hasComma) {
	                    lastName = personWordsList.get(0);
	                    for (int i = 1; i < personWordsList.size(); i++) {
//						System.out.println("Adding: " + personWordsList.get(i));
	                        givenNames += personWordsList.get(i);
	                        if (i < personWordsList.size() - 1) {
	                            givenNames += " ";
	                        }
	                    }
	                } else {
	                    lastName = personWordsList.get(0);
	                    for (int i = 0; i < personWordsList.size() - 1; i++) {
//						System.out.println("Adding: " + personWordsList.get(i));
	                        givenNames += personWordsList.get(i);
	                        if (i < personWordsList.size() - 2) {
	                            givenNames += " ";
	                        }
	                    }
	                    lastName = personWordsList.get(personWordsList.size() - 1);
	                }
	            } else if (personWordsList.size() == 1) {
	                givenNames = personWordsList.get(0);
	            }
//			System.out.println("Given names: " + givenNames);
//			System.out.println("Last name: " + lastName);
	            String formattedName = givenNames + lastName;
	            formattedName = formattedName.replaceAll("\\s", "");
//	            System.out.println("Formatted name: "+formattedName);
	            return formattedName;
	}
	
	public boolean isDeceased(String name) {
		name=this.formatName(name);
		for (String[] truthLine : truths) {
			if (truthLine.length > 6) {
				if (truthLine[6].equals("Deceased")) {
					if (truthLine.length > 21) {
						String truthName=truthLine[21] + " "+truthLine[20];
						truthName.replaceAll("-", "").replaceAll("'", "");
						String[] truthNameSplit=truthName.toLowerCase().split("\\s");
                        String lowerName = name.toLowerCase();
                        int matches = 0;
                        for(int i = truthNameSplit.length-1; i >= 0; i--)
                        {
                            String truthSubName = truthNameSplit[i];
                            if(lowerName.contains(truthSubName))
                            {
                                matches++;
                            }
                        }
                        if(matches > 1)
                        {
                            return true;
                        }
                        truthName = truthName.toLowerCase().replace("\\s","");
//						System.out.println("truthname: "+truthName+" name: "+name);
						if (truthName.equals(name)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean isMultDec() {
		int decCount = 0;
		if (truths != null) {
			for (String[] truthLine : truths) {
				if (truthLine.length > 6) {
					if (truthLine[6].equals("Deceased")) {
						decCount++;
						if (truthLine.length > 21) {
							deceased.add(truthLine[20] + truthLine[21]);
						}
					}
				}
			}
		}
		if (decCount > 1) {
			return true;
		}
		return false;
	}

	public String getId() {
		// System.out.println(id);
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<String[]> getTruths() {
		return truths;
	}

	public void setTruths(ArrayList<String[]> truths) {
		this.truths = truths;
	}

	public void addTruth(String[] truthsLine) {
		this.truths.add(truthsLine);
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTaggedBody() {
		return taggedBody;
	}

	public void setTaggedBody(String taggedBody) {
		this.taggedBody = taggedBody;
	}

	public List<String[]> getNames() {

		return names;
	}

	public void setNames(List<String[]> names) {
		this.names = names;
	}

	public ArrayList<String> getDeceased() {
		return deceased;
	}

	public void setDeceased(ArrayList<String> deceased) {
		this.deceased = deceased;
	}

	public void addPerson(String person, boolean format) {
		if(format) {
          
        }
        else
        {
            person = person.replaceAll("&#034;", "\\\"").replaceAll("&#039;", "\\\'").replaceAll("&#151;", "-");
            String[] personWords = person.split("\\P{L}+");
            if(personWords.length < 2)
            {
                return;
            }
            String deceased = "0";
            if (isDeceased(person)) {
                deceased = "1";
            }
            String[] personPair = {person, deceased};
            names.add(personPair);
        }
	}

	public List<String> getWordListOfBody() {
        Map<String, String> nameMap = new LinkedHashMap<String, String>();
        String bodyCopy = this.body;
        for(String[] nameObj : this.names)
        {
            String name = nameObj[0];
            String noSpaceName = name.replaceAll("\\s", "").replace(",", "").replace(".", "");
            nameMap.put(noSpaceName, nameObj[0]);
            bodyCopy = bodyCopy.replace(name, noSpaceName);
        }
        bodyCopy = bodyCopy.replaceAll("[[,]|[.]|[;]|[:]|[\']|[\"]]+", "");
        bodyCopy = bodyCopy.replaceAll("\\s+", "|");
        Iterator it = nameMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            bodyCopy = bodyCopy.replace((String) pair.getKey(), ((String) pair.getValue()).replaceAll(",|'|\"", ""));
        }
        List<String> wordList = Arrays.asList(bodyCopy.split("\\|"));
        return wordList;
	}

}
