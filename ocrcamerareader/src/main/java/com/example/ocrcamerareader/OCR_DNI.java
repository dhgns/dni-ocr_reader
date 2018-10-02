package com.example.ocrcamerareader;

import android.os.Bundle;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by dhernandez on 01/10/2018.
 */

public class OCR_DNI {


    public enum Gender {MALE, FEMALE}

    String documentName;
    String nationality;
    String documentNumber;
    Date expirationDate;
    Date birthDate;
    Gender gender;

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public void setExpirationDate(String expYear, String expMonth, String expDay) {
        this.expirationDate = parseDate(expYear + expMonth + expDay);
    }

    public void setBirhtDate(String year, String month, String day) {
        this.birthDate = parseDate(year + month + day);
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    private Date parseDate(String s) {
        DateFormat formatter;
        Date parsedDate;
        Date date1 = null;

        formatter = new SimpleDateFormat("yyMMdd");

        try {
            date1 = formatter.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return date1;
    }

    public void setGender(String gender) {
        if(gender.toUpperCase().equals("M"))
            this.gender = Gender.MALE;
        else if(gender.toUpperCase().equals("F"))
            this.gender = Gender.FEMALE;
        else
            this.gender = null;

    }

    public Bundle getBundle() {
        Bundle ret = new Bundle();

        ret.putString("DOCUMENT_NATIONALITY", nationality);
        ret.putString("DOCUMENT_NUMBER", documentNumber);
        ret.putString("DOCUMENT_EXPIRATION_DATE", expirationDate.toString());
        ret.putString("DOCUMENT_BIRTHDATE", birthDate.toString());
        ret.putString("DOCUMENT_GENDER", gender.toString());

        return ret;
    }

    public void setName(ArrayList<String> name) {
        String aux = "";
        for (String s : name) {
            aux = aux + " " + s;
        }
        this.documentName = aux;
    }

}
