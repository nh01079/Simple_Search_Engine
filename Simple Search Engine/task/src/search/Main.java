package search;
import javafx.beans.binding.IntegerBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    enum MATCH_STRATEGY {
        ALL, ANY, NONE
    }
    static  class invertedIndex {
        Map<String, Set<Integer>> map = new HashMap<>();
        void add (String text, Integer pos) {
            if (!map.containsKey(text)) {
                map.put(text, new HashSet<>());
            }
            map.get(text).add(pos);
        }

        Set<Integer> get (String text) {
            return map.getOrDefault(text, new HashSet<>());
        }

        void addPerson(Person person, Integer pos){
            this.add(person.firstName.toLowerCase(), pos);
            this.add(person.lastName.toLowerCase(), pos);
            if(person.email != null) this.add(person.email, pos);
        }

        Set<Integer> searchStrategy(String text, MATCH_STRATEGY strategy) {
            switch (strategy) {
                case ALL:
                    return this.get(text);
                case ANY:
                case NONE:
                    Set<Integer> set = new LinkedHashSet<>();
                    for(String pattern: text.split("\\s+")) {
                        System.out.println(pattern);
                        set.addAll(this.get(pattern));
                    }
                    return set;
            }
            return null;
        }
    }

    static class Person{
        String firstName;
        String lastName;
        String email;

        Person(String firstName, String lastName){
            this(firstName, lastName, "");
        }

        Person(String firstName, String lastName, String email){
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        @Override
        public String toString(){
            return String.format("%s %s %s", firstName, lastName, email).trim();
        }

        // Factory method
        static Person parseRawString(String text){
            String[] param = text.split("\\s");
            if(param.length == 2) {
                return new Person(capitalize(param[0]), capitalize(param[1]));
            } else if (param.length == 3) {
                return new Person(capitalize(param[0]), capitalize(param[1]), param[2]);
            } else return null;
        }

        // Helper method
        private static String capitalize(String text) {
            text = text.toLowerCase();
            return text.substring(0,1).toUpperCase() + text.substring(1);
        }

        // static search method
        static Person[] searchPerson(Person[] array, String pattern) {
            Queue<Person> match = new LinkedList<>();
            pattern = pattern.toLowerCase();
            for (Person person : array) {
                if (person.toString().toLowerCase().contains(pattern)) {
                    match.add(person);
                }
            }
            return match.toArray(new Person[0]);
        }

    }

    public static void main(String[] args) {
        if (args.length != 2 || !args[0].equals("--data")) {
            return;
        }
        Person[] persons;
        invertedIndex index = new invertedIndex();
        Scanner scanner = new Scanner(System.in);
        try (Scanner document = new Scanner(new File(args[1]))){
            List<Person> temp = new LinkedList<>();
            int pos = 0;
            while (document.hasNext()) {
                Person current = Person.parseRawString(document.nextLine());
                if (current != null) {
                    temp.add(current);
                    index.addPerson(current, pos);
                    pos++;
                }
            }
            persons = temp.toArray(new Person[0]);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
            return;
        }
        //System.out.println(Arrays.toString(persons));
        boolean exit = false;
        while (!exit) {
            System.out.println("=== Menu ===");
            System.out.println("1. Find a person\n2. Print all people\n0. Exit");
            switch (Integer.parseInt(scanner.nextLine())) {
                case 0: // exit
                    exit = true;
                    System.out.println("Bye!");
                    break;
                case 1: // find a person
                    System.out.println("Select a matching strategy: ALL, ANY, NONE");
                    String strategy = scanner.nextLine().trim().toUpperCase();
                    String line = scanner.nextLine();
                    Set<Person> result = new LinkedHashSet<>();
                    switch (strategy) {
                        case "ALL" :
                            index.searchStrategy(line, MATCH_STRATEGY.ALL).forEach(pos -> result.add(persons[pos]));
                            break;
                        case "ANY" :
                            index.searchStrategy(line, MATCH_STRATEGY.ANY).forEach(pos -> result.add(persons[pos]));
                            break;
                        case "NONE":
                            result.addAll(Arrays.asList(persons));
                            index.searchStrategy(line, MATCH_STRATEGY.NONE).forEach(pos -> result.remove(persons[pos]));
                            break;
                        default:
                            System.out.println("Invalid strategy");
                    }
                    //Collections.addAll(set, Person.searchPerson(persons, line));
                    //System.out.println(result.size());
                    if (result.isEmpty()) {
                        System.out.println("Person not found");
                    } else {
                        for(Person person: result) {
                            System.out.println(person);
                        }
                    }
                    break;
                case 2:
                    //System.out.println(persons.length);
                    for(Person person: persons) {
                        System.out.println(person);
                    }
                    break;
                default:
                    System.out.println("Incorrect option! Try again.");
            }
        }

    }

}
