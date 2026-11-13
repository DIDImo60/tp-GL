package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentAccount{
    
    //اجباري
    private String id;
    private String fullName;
    private String email;
    //اختياري

    private String phone;
    private String level;
    private String major;
    private Boolean scholship;
    private List<String> courses;

    //منشئ مع builder
    private StudentAccount (StudentAccountBuilder builder){
        this.id = builder.id;
        this.fullName = builder.fullName;
        this.email = builder.email;
        this.phone = builder.phone;
        this.level = builder.level;
        this.major = builder.major;
        this.scholship = builder.scholship;
        this.courses = builder.courses;
    }

    //getting
    public String getid(){return this.id;}
    public String getfullName(){return this.fullName;}
    public String getemail(){return this.email;}
    public String getphone(){return this.phone;}
    public String getlevel(){return this.level;}
    public String getmajor(){return this.major;}
    public Boolean getscholship(){return this.scholship;}
    public List<String> getcourses(){return Collections.unmodifiableList(courses);}


    //builder
    public static class StudentAccountBuilder {
        //اجباري
        private String id;
        private String fullName;
        private String email;
        
        //اختياري
        private String phone = null;
        private String level = "l1";
        private String major = "";
        private Boolean scholship = false;
        private List<String> courses = new ArrayList<>();
        //منشئ
        public StudentAccountBuilder(String id, String fullName, String email){
            this.id = id;
            this.fullName = fullName;
            this.email = email;
        }
        
        // تعديل القيم الاختيارية
        public StudentAccountBuilder phone(String phone){
            this.phone = phone;
            return this;
        }

        public StudentAccountBuilder level(String level){
            this.level = level;
            return this;
        }

        public StudentAccountBuilder major(String major){
            this.major = major;
            return this;
        }

        public StudentAccountBuilder scholship(Boolean scholship){
            this.scholship = scholship;
            return this;
        }

        public StudentAccountBuilder courses(List<String> courses){
            if(courses != null){
                this.courses = new ArrayList<>(courses);
            }
            return this;
        }

        public StudentAccount build(){
            return new StudentAccount(this);
        }

    }
}