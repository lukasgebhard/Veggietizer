package freerunningapps.veggietizer.model;

import android.annotation.SuppressLint;
import freerunningapps.veggietizer.model.enums.Meat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements a meat dish.
 *
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 *
 */
public class MeatDish {
    private int id;
    
    /**
     * The date on which the dish is consumed.
     */
    private Date date;
    
    private Meat meat;
    
    /**
     * The amount in grammes.
     */
    private int amount;

    /**
     * Creates a meat dish.
     * 
     * @param id The ID.
     * @param date The date on which it is consumed.
     * @param meat The sort of meat.
     * @param amount The amount in grammes.
     */
    public MeatDish(int id, Date date, Meat meat, int amount) {
        this.id = id;
        this.date = date;
        this.meat = meat;
        this.amount = amount;
    }

    @SuppressWarnings("unused")
    public int getId() {
        return id;
    }

    /**
     * @return The date on which the dish is consumed.
     */
    @SuppressWarnings("unused")
    public Date getDate() {
        return date;
    }

    public Meat getMeat() {
        return meat;
    }

    /**
     * @return The amount in grammes.
     */
    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return "Meat dish [ID" + id + ", " + dateFormat.format(date) + ", " + meat + ", " + amount + "g]";
    }
}
