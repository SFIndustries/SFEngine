package com.example.luka.openglestest.util;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Luka on 17.11.2015..
 */
public class TextResourceReader
{
    public static String readTextFileFromResource(Context context, int resourceId)
    {
        StringBuilder body = new StringBuilder();
        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader =new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: " + resourceId, nfe);
        }
        return body.toString();
    }

    public static List LoadObjFromResource(Context context, int resourceId)
    {
        Vector vrhovi = new Vector();
        Vector normale = new Vector();
        Vector UV = new Vector();

        Vector tempVrhovi = new Vector();
        Vector tempNormale = new Vector();
        Vector tempUV = new Vector();

        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader =new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {

                String[] parts = nextLine.split(" ");
                if (parts[0].equals("v"))
                {
                    tempVrhovi.add(Float.parseFloat(parts[1]) );
                    tempVrhovi.add(Float.parseFloat(parts[2]) );
                    tempVrhovi.add(Float.parseFloat(parts[3]) );
                }
                else if (parts[0].equals("vn"))
                {
                    tempNormale.add(Float.parseFloat(parts[1]) );
                    tempNormale.add(Float.parseFloat(parts[2]) );
                    tempNormale.add(Float.parseFloat(parts[3]) );
                }
                else if (parts[0].equals("vt"))
                {
                    tempUV.add(Float.parseFloat(parts[1]) );
                    tempUV.add(Float.parseFloat(parts[2]) );
                }
                else if (parts[0].equals("f"))
                {
                    String[] parts1 = parts[1].split("/");
                    String[] parts2 = parts[2].split("/");
                    String[] parts3 = parts[3].split("/");

                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts1[0])-1)*3 )  );
                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts1[0])-1)*3+1 )  );
                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts1[0])-1)*3+2 )  );

                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts2[0])-1)*3 )  );
                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts2[0])-1)*3+1 )  );
                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts2[0])-1)*3+2 )  );

                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts3[0])-1)*3 )  );
                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts3[0])-1)*3+1 )  );
                    vrhovi.add(  tempVrhovi.elementAt( (Integer.parseInt(parts3[0])-1)*3+2 )  );

                    // V koordinata je INVERTIRANA !!!
                    UV.add(  tempUV.elementAt( (Integer.parseInt(parts1[1])-1)*2 )  );
                    UV.add(  1 - (float) tempUV.elementAt( (Integer.parseInt(parts1[1])-1)*2+1 )  );

                    UV.add(  tempUV.elementAt( (Integer.parseInt(parts2[1])-1)*2 )  );
                    UV.add(  1 - (float) tempUV.elementAt( (Integer.parseInt(parts2[1])-1)*2+1 )  );

                    UV.add(  tempUV.elementAt( (Integer.parseInt(parts3[1])-1)*2 )  );
                    UV.add(  1 - (float) tempUV.elementAt( (Integer.parseInt(parts3[1])-1)*2+1 )  );


                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts1[2])-1)*3 )  );
                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts1[2])-1)*3+1 )  );
                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts1[2])-1)*3+2 )  );

                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts2[2])-1)*3 )  );
                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts2[2])-1)*3+1 )  );
                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts2[2])-1)*3+2 )  );

                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts3[2])-1)*3 )  );
                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts3[2])-1)*3+1 )  );
                    normale.add(  tempNormale.elementAt( (Integer.parseInt(parts3[2])-1)*3+2 )  );
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: " + resourceId, nfe);
        }

        List lista = new ArrayList();


        float[] floatArray = new float[vrhovi.size()];
        for (int i=0; i<vrhovi.size(); i++)
        {
            floatArray[i] = (float) vrhovi.elementAt(i);
        }
        lista.add(floatArray);

        floatArray = new float[normale.size()];
        for (int i=0; i<normale.size(); i++)
        {
            floatArray[i] = (float) normale.elementAt(i);
        }
        lista.add(floatArray);

        floatArray = new float[UV.size()];
        for (int i=0; i<UV.size(); i++)
        {
            floatArray[i] = (float) UV.elementAt(i);
        }
        lista.add(floatArray);

        return lista;
        //return floatArray;

    }

    public static Vector LoadCollisionObject(Context context, int resourceId)
    {
        Vector vertices = new Vector(), allVertices = new Vector();
        Vector objects = new Vector();

        float[] currentVertex = new float[3];
        float[][] floatArray2;
        float[][][] floatArray3;

        try {

            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader =new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null)
            {
                String[] parts = nextLine.split(" ");

                if (parts[0].equals("g"))
                {
                    if ( vertices.isEmpty() )
                        continue;

                    floatArray2 = new float[vertices.size()][3];
                    for (int i=0; i<vertices.size(); i++)
                    {
                        floatArray2[i] = (float[]) (vertices.elementAt(i));
                    }
                    objects.add( floatArray2 );
                    vertices.clear();
                }

                if (parts[0].equals("v"))
                {
                    currentVertex = new float[3];

                    currentVertex[0] = Float.parseFloat(parts[1]);
                    currentVertex[1] = Float.parseFloat(parts[2]);
                    currentVertex[2] = Float.parseFloat(parts[3]);

                    vertices.add(currentVertex);
                    allVertices.add(currentVertex);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        floatArray2 = new float[vertices.size()][3];
        for (int i=0; i<vertices.size(); i++)
        {
            floatArray2[i] = (float[]) (vertices.elementAt(i));
        }
        objects.add( floatArray2 );

        // cijeli objekt
        floatArray2 = new float[allVertices.size()][3];
        for (int i=0; i<allVertices.size(); i++)
        {
            floatArray2[i] = (float[]) (allVertices.elementAt(i));
        }
        objects.add( floatArray2 );

        return objects;
    }

}
