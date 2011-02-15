
package org.biofab.playground;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;

public class SequenceAnnotator
{
    String      _jdbcDriver = "jdbc:postgresql://localhost:5432/biofab";
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

        try
        {
            _connection = DriverManager.getConnection(_jdbcDriver, _user, _password);
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
                        System.out.println(designID + "," + featureID + "," + String.valueOf(start) + "," + String.valueOf(stop));
                    }
                }

                features.beforeFirst();
            }
        }
        catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
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
