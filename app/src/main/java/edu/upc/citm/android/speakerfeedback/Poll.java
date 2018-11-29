package edu.upc.citm.android.speakerfeedback;

import java.util.Date;
import java.util.List;

public class Poll
{
    private String hash_question;
    private String question;
    private boolean open;
    private Date start;
    private Date end;
    private List<String> options;
    private List<Integer> results;

    Poll(){}

    public String getQuestion()
    {
        return question;
    }

    public List<String> getOptions()
    {
        return options;
    }

    public String getOptionsAsString()
    {
        StringBuilder b = new StringBuilder();

        for(String op : options)
        {
            b.append(op);
            b.append("\n");
        }
        return b.toString();
    }

    public boolean isOpen()
    {
        return open;
    }

    public Date getStart()
    {
        return start;
    }

    public void setStart(Date start)
    {
        this.start = start;
    }

    public Date getEnd()
    {
        return end;
    }

    public void setEnd(Date end)
    {
        this.end = end;
    }

    public String getHash_question()
    {
        return hash_question;
    }

    public void setHash_question(String hash_question)
    {
        this.hash_question = hash_question;
    }
}