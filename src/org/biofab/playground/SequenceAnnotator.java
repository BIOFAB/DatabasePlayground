
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
    String      _jdbcDriver = "jdbc:postgresql://localhost:5432/biofab_dev";
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
            designs = designsStatement.executeQuery("select * from design");
            features = featuresStatement.executeQuery("SELECT feature.id, feature.dna_sequence FROM feature WHERE feature.id = 45 OR feature.id = 83 OR feature.id = 84 ORDER BY feature.id ASC");
            System.out.println("design_id,feature_id,start,stop");

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
                        start = matcher.start();
                        stop = matcher.end();
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
