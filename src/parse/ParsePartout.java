package parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*ParsePartout*/
public class ParsePartout {
    
	private static String nameFile;
	
	
    public static StringBuilder pdfToText(String filepath) {
        StringBuilder text = new StringBuilder();
    	try {
    		//commande console, encodage --> Ascii7 permet la gestion des accents
	    	String[] command = {"pdftotext","-enc","ASCII7", filepath, "-"};
	        
	    	//execution de la commande
	        Process process = Runtime.getRuntime().exec(command);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        while ((line = reader.readLine()) != null) {
	            text.append(line).append("\n");
	        }
	        process.waitFor();
    	} catch (IOException | InterruptedException e) {
    		e.printStackTrace();
    	}
    	//retourne le texte en vrac
    	return text;
    }

    public static ArrayList<String> getAuteur(String texte) {
    	
        ArrayList<String> potentialAuthors = new ArrayList<>();
        Pattern pattern = Pattern.compile("[A-Z][a-z]+(-[A-Z][a-z]+)?( ([A-Z].)+)?( [a-z]*)? [A-Z]([A-Z]|[a-z])+(-[A-Z][a-z]+)?");

        Matcher matcher = pattern.matcher(texte.substring(20, Math.min(texte.length(), 300)));
        
        while (matcher.find()) {
        	//System.out.println(matcher.group());
            potentialAuthors.add(matcher.group());
        }
       
       Pattern firstnamePattern = Pattern.compile("[A-Z][a-z]+(-[A-Z][a-z]+)?");
       Pattern midnamePattern = Pattern.compile("( ([A-Z].)+)?( [a-z]*)?");
       Pattern lastnamePattern = Pattern.compile("( ([A-Z].)+)?( [a-z]*)? [A-Z]([A-Z]|[a-z])+(-[A-Z][a-z]+)?");
        
        
        //compteur email
        HashMap<String, Integer> compteur = new HashMap<>();
        for (String str : potentialAuthors) {
            compteur.put(str, 0);
        }
        
        StringTokenizer tokenizer = new StringTokenizer(texte, "\n");
        String previousline="";
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            if (line.contains("@")) {
            	int cbArr=0;
	            for(int i=0; i<line.length();i++) {
	            	if(line.charAt(i)=='@') cbArr++;
	            }
	            if(cbArr>1) {
	            	String[] lineSplit = line.split(" ");
	            	for(int i =0;i<cbArr;i++) {
			            for (String potAuthor : potentialAuthors) {
			            	
							Matcher matcherFirstname = firstnamePattern.matcher(potAuthor);
							Matcher matcherMidname = midnamePattern.matcher(potAuthor);
				            Matcher matcherLastname = lastnamePattern.matcher(potAuthor);
				            String firstname = null;
				            if (matcherFirstname.find()) {
				            	firstname = matcherFirstname.group();
				            }
				            String midname = null;
				            if (matcherMidname.find()) {
				            	midname = matcherMidname.group();
				            }
				            String lastname = null;
				            if (matcherLastname.find()) {
				            	lastname = matcherLastname.group();
				            }
				            int indexArr=(lineSplit[i].indexOf("@")==0)? lineSplit[i].length() : lineSplit[i].indexOf("@") ;
				            if (lineSplit[i].substring(0,indexArr).contains(firstname.toLowerCase()) ||lineSplit[i].substring(0,indexArr).contains(lastname.substring(1).toLowerCase())){
				            	int count = compteur.get(potAuthor);
							    compteur.put(potAuthor, count + 1);
				            }
				        }		        	
	            	}
	            }
	            else {
	            	for (String potAuthor : potentialAuthors) {
		            	ArrayList<String> variableAuthor = new ArrayList<String>();

						Matcher matcherFirstname = firstnamePattern.matcher(potAuthor);
						Matcher matcherMidname = midnamePattern.matcher(potAuthor);
			            Matcher matcherLastname = lastnamePattern.matcher(potAuthor);
			            String firstname = null;
			            if (matcherFirstname.find()) {
			            	firstname = matcherFirstname.group();
			            }
			            String midname = null;
			            if (matcherMidname.find()) {
			            	midname = matcherMidname.group();
			            }
			            String lastname = null;
			            if (matcherLastname.find()) {
			            	lastname = matcherLastname.group();
			            }
//			            System.out.println(firstname);
//			            System.out.println(lastname);
//			            String initialName="afm";
			            //String initialName = firstname.substring(0,1)+midname.substring(0,1)+lastname.substring(0,1);
			            int indexArr=(line.indexOf("@")==0)? line.length() : line.indexOf("@") ;
			          	int indexArrPrevious = (previousline.indexOf("@")==-1)? previousline.length() : previousline.indexOf("@") ;
				        if (line.substring(0,indexArr).contains(firstname.toLowerCase()) || line.substring(0,indexArr).contains(lastname.substring(1).toLowerCase()) || previousline.substring(0,indexArrPrevious).contains(firstname.toLowerCase()) || previousline.substring(0,indexArrPrevious).contains(lastname.substring(1).toLowerCase())) { //|| line.contains(initialName.toLowerCase())){
				            	
				        	int count = compteur.get(potAuthor);
						    compteur.put(potAuthor, count + 1);
				        }
	            	}
	            }
	        }
            previousline=line;
        }
        
        ArrayList<String>author=new ArrayList<String>();
        for (Map.Entry<String,Integer> m : compteur.entrySet()) {

        	//System.out.println("clé: "+m.getKey() + " | valeur: " + m.getValue());
        	if(m.getValue()>=1) {
        		System.out.println("Auteur : " + m.getKey());
        		author.add(m.getKey());
        	}
        }
        return author;
        
    
      
    }
    public ArrayList<String> getVariableAuthor(String firstname, String midname, String lastname){
    	ArrayList<String> variableAuthor = new ArrayList<String>();
    	return variableAuthor;
    	
    }
	public static String getTitre(String texte) {
		//methode pour retourner le titre du document
		return texte.split("\n")[0];
	}
	public static String getNom(File f) {
		//return nom du fichier 
		return f.getName();
	}
	public static String getAbstract(String texte) {
		String[] lines = texte.split("\n");
		String retour = null;
		
		for(int i=0; i<lines.length; i++) {
			//Si l'abtrsact est en gros titre
			if(lines[i].toUpperCase().equals("ABSTRACT")) {
				retour = lines[i+1];
				break;
			//si le mot abstract est compris dans l'abstract
			}else if(lines[i].toUpperCase().contains("ABSTRACT")) {
				//on enleve le mot abstract
				for(int j=1; j<lines[i].length(); j++) {
					if(lines[i].charAt(j)>='A' && lines[i].charAt(j)<='Z' ) {
						retour = lines[i].substring(j);
						break;
					}
				}
				break;
			}
		}
		
		//Si il ny a pas le mot Abstract
		if(retour==null) {
			for(int i=0; i<lines.length; i++) {		
				if(lines[i].toUpperCase().contains("INTRODUCTION")) {
					if(lines[i-1].equals(""))
						retour = lines[i-2];
					else
						retour = lines[i-1];
					break;
				}
			}
		}	
		return retour;
	}
	public static File creationFichierSansRename(File f) {
		//creation du fichier texte sans rename
		File file = new File("./DejaParséAlorsTuVasFaireQuoi/"+f.getName().substring(0,f.getName().length()-4) + ".txt");
		
		//verifie la creation
		if(file.exists()) {
			try {
				file.createNewFile();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	public static File creationFichierAvecRename(File f,String rename) throws IOException {
		//crée un fichier dans le dossier préparé + le rename selon le parametre
		File file = new File("./DejaParséAlorsTuVasFaireQuoi/"+rename+ ".txt");
		
		//verifie la creation
		if(file.exists()) {
			try {
				file.createNewFile();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	public static void putInfo(File from, File out) throws IOException {
		//writer
		FileWriter fw = new FileWriter(out);
		BufferedWriter bw = new BufferedWriter(fw);
		
		//texte en vrac
		String block= getString(from.getName());
		
		//on extraie les variables
		String titre = getTitre(block);
		ArrayList<String> auteurs = getAuteur(block);
		String abstrac = getAbstract(block);
		
		//creation du texte
		bw.append("Nom du fichier :\n");
		bw.append("			"+from.getName()+"\n");
		
		if(!titre.equals("")) {
			bw.append("Titre :\n");
			bw.append("			"+titre+"\n");
		}

		if(auteurs.size()==1) {
			bw.append("\nAuteur :\n");
			bw.append("			"+auteurs.get(0));
		}
		if(auteurs.size()>1) {
			bw.append("\nAuteurs :\n");
			for(String a : auteurs) {
				bw.append("			"+a);
			}
		}
		if(abstrac!=null) {
			bw.append("\nAbstract :\n");
			bw.append(abstrac);	
		}		
		bw.close();
		fw.close();
		
	}
	public static String getString(String nom) {
		//recupere et retourne le texte en vrac
    	String homeDirectory = System.getProperty("user.dir");
    	String filePath = Paths.get(homeDirectory, "Corpus_2021").toString();
		StringBuilder t = pdfToText(filePath+"/"+nom);
		return t.toString();
	}
    public static void main(String args[]) throws IOException {
    	
    	String homeDirectory = System.getProperty("user.dir");
    	String filePath = Paths.get(homeDirectory, "Corpus_2021").toString();
    	File directory = new File(filePath);
        // Parcourez les fichiers du répertoire
        File[] files = directory.listFiles();
        File dir = new File("./DejaParséAlorsTuVasFaireQuoi");
        dir.mkdir();
        if (files != null) {
            for (File file : files) {
            	System.out.println(file.getName());
                if (file.isFile() && file.getName().endsWith(".pdf")) {
                    putInfo(file, creationFichierSansRename(file));
                }
            }
        }
    }
}
