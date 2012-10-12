package net.medcommons.application.dl;





/**
 * Represents a person. Persons can be patients, clerks, or doctors.
 *
 * @author mesozoic
 *
 */
public class Person {
	private Integer id; // Should disappear? Is this an internal table index or a real identifier?
	 
	    private String username;
	    private String firstName;
	    private String lastName;
	   
	    private String title;

	    /** Default constructor. */
	    public Person() { }

	    /** Constructs a well formed person. */
	    public Person(String username, String first, String last,  String title) {
	        this.username = username;
	        this.firstName = first;
	        this.lastName = last;
	        this.title = title;
	    }
	  
	    

	   
	    /** Gets the username of the person. */
	    public String getUsername() { return username; }

	    /** Sets the username of the user. */
	    public void setUsername(String username) { this.username = username; }

	    /** Gets the first name of the person. */
	    public String getFirstName() { return firstName;  }

	    /** Sets the first name of the user. */
	    public void setFirstName(String firstName) { this.firstName = firstName; }

	    /** Gets the last name of the person. */
	    public String getLastName() { return lastName; }

	    /** Sets the last name of the user. */
	    public void setLastName(String lastName) { this.lastName = lastName; }

	  
	    public void setTitle(String title){
	    	this.title = title;
	    }

	    public String getTitle(){
	    	return(this.title);
	    }
	    /** Equality is determined to be when the ID numbers match. */
	    public boolean equals(Object obj) {
	        return (obj instanceof Person) && this.id == ((Person) obj).id;
	    }
	    /** Gets the ID of the person. */
	    public Integer getId() { return id; }

	    /** Sets the ID of the person. */
	    public void setId(Integer id) { this.id = id; }
}