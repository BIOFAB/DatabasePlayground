
package org.biofab.playground;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class SequenceAnnotator
{
    String      _jdbcDriver = "jdbc:postgresql://localhost:5432/biofabm";
    String      _user = "biofab";
    String      _password = "fiobab";
    Connection  _connection = null;
    
    public static void main(String[] args)
    {
        SequenceAnnotator p = new SequenceAnnotator();
                    

    }

    public SequenceAnnotator()
    {
        Statement       designsStatement = null;
        Statement       featuresStatement;
        ResultSet       designs = null;
        ResultSet       features = null;
        String          designSeq = null;
        String          featureSeq = null;
        Pattern         pattern = null;
        Matcher         matcher = null;
        int             start;
        int             stop;
        String          designID;
        String          featureID;

        ArrayList<OneFeature> all_features = new ArrayList<OneFeature>();

        try
        {
            Properties props = new Properties();
            props.setProperty("user", _user);
            props.setProperty("password", _password);
            //props.setProperty("ssl", "true");
            _connection = DriverManager.getConnection(_jdbcDriver, props);
            designsStatement = _connection.createStatement();
            featuresStatement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            designs = designsStatement.executeQuery("SELECT design.id, design.dna_sequence FROM design WHERE design.dna_sequence != 'pending'");
            features = featuresStatement.executeQuery("SELECT feature.id, feature.dna_sequence FROM feature ORDER BY feature.id ASC");

            while (designs.next())
            {
                designID = designs.getString("id");
                designSeq = designs.getString("dna_sequence");


                while(features.next())
                {
                    featureID = features.getString("id");
                    featureSeq = features.getString("dna_sequence");
                    pattern = Pattern.compile(featureSeq, Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(designSeq);

                    while(matcher.find())
                    {
                        start = matcher.start() + 1;
                        stop = matcher.end() + 1;

                        OneFeature feat = new OneFeature(designID, featureID, featureSeq, start, stop);
                        all_features.add(feat);


                        //System.out.println(designID + "," + featureID + "," + String.valueOf(start) + "," + String.valueOf(stop));
                    }
                }

                features.beforeFirst();
            }

            ArrayList<OneFeature> accepted = new ArrayList<OneFeature>();




            boolean accept;
            OneFeature outer;
            OneFeature inner;
            Iterator inner_iter;
            Iterator outer_iter = all_features.iterator();
            while(outer_iter.hasNext()) {
                outer = (OneFeature) outer_iter.next();
                accept = true;
                inner_iter = all_features.iterator();

                while(inner_iter.hasNext()) {
                    inner = (OneFeature) inner_iter.next();

                    if(inner.getSeq().equals(outer.getSeq())) {
                        continue;
                    }

                    if((inner.getStart() > outer.getStart()) && (inner.getEnd() <= outer.getEnd())) {

                        accept = false;
                    }
                    if((inner.getStart() >= outer.getStart()) && (inner.getEnd() < outer.getEnd())) {
                        accept = false;
                    }
                    if(!accept) {
                        System.out.println("Found a seq within a seq: " + outer.getSeq() + " - " + inner.getSeq());
                    }

                }
                if(accept) {
                    accepted.add(outer);
                }
            }
            OneFeature cur;
            Iterator acc_iter = accepted.iterator();
            while(acc_iter.hasNext()) {
                cur = (OneFeature) acc_iter.next();
                System.out.println(cur.getDesign_id() + "," + cur.getDbid() + "," + String.valueOf(cur.getStart()) + "," + String.valueOf(cur.getEnd()));
            }

            

        }
        catch (SQLException ex)
        {
            System.out.println("AAA: " + ex.getMessage());
        }
        finally
        {
            try
            {
                _connection.close();
            }
            catch (SQLException ex)
            {
                System.out.println(ex.getMessage());
            }
        }

        System.out.println("Done");
    }
}
