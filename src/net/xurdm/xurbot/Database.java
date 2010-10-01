package net.xurdm.xurbot;
import java.util.*;
import java.io.*;

/**
 *
 * @author Ryan
 */
public class Database {
    private Map<String,Map<String,Object>> db;
    private String fileName;

    public Database() {
        db = new HashMap<String,Map<String,Object>>();
    }

    public void add(String table, String key, Object value) {
        db.get(table).put(key, value);
    }

    public Map<String,Object> getTable(String table) {
        return db.get(table);
    }

    public Object getValue(String table, String key) {
        return db.get(table).get(key);
    }

    public boolean containsKey(String table, String key) {
        return db.get(table).containsKey(key);
    }

    public boolean containsValue(String table, String value) {
        return db.get(table).containsValue(value);
    }

    public boolean containsTable(String table) {
        return db.containsKey(table);
    }

    public void addTable(String table) {
        db.put(table, new HashMap<String,Object>());
    }
    
    public Object popKey(String table) {
        java.util.Iterator it = db.get(table).keySet().iterator();
        Object key;
        if(!db.get(table).isEmpty() && it.hasNext()) {
            key = it.next();
            db.get(table).remove(key.toString());
            return key;
        }
        return null;
    }

    public Set<String> getKeys(String table) {
        if(!db.get(table).isEmpty())
            return db.get(table).keySet();
        return null;
    }

    public String containsKeyMatch(String table, String match, boolean regexMatch) {
        for(Map.Entry<String,Map<String,Object>> entry : db.entrySet())
            if(regexMatch) {
                if(entry.getKey().matches(match))
                    return entry.getKey();
            } else {
                if(match.matches(entry.getKey().replace("*",".*")))
                    return entry.getKey();
            }
        return null;
    }

    public Object containsValueMatch(String table, String match, boolean regexMatch) {
        for(Map.Entry<String, Object> entry : db.get(table).entrySet())
            if(regexMatch) {
                if(entry.getValue().toString().matches(match))
                    return entry.getValue();
            } else {
                if(match.matches(entry.getValue().toString().replace("*",".*")))
                    return entry.getValue();
            }
        return null;
    }

    public void load(String name) {
        File file = new File(String.format("%s%s",name,name.endsWith(".txt")?"":".txt"));
        BufferedReader br;
        String line, table, set, temp = "";
        Map<String, Object> map;

        if(!file.exists())
            return;
        try {
            br = new BufferedReader(new FileReader(file));
            db = new HashMap<String, Map<String,Object>>();
            fileName = file.getName();
            while((line = br.readLine()) != null) {
                int j;
                map = new HashMap<String,Object>();
                table = line.split("#")[0];
                set = line.split("#")[1];
                j = set.replaceAll("[^\\$]","").length()+1;
                for(int k = 0; k < j; k++) {
                    temp = set.split("\\$")[k];
                    System.out.println(temp);
                    map.put(temp.split("&")[0],temp.split("&")[1]);
                    db.put(table, map);
                }
            }
            System.out.println(db.toString());
            br.close();
        } catch (IOException e) {
            //ONLY LOSERS CATCH EXCEPTIONS THE RIGHT WAY
            e.printStackTrace();
        }
    }

    public void save() {
        save(fileName);
    }

    public void saveActivity(String fileName) {

    }

    public void save(String fileName) {
        File file = new File(fileName);
        if(file.exists())file.delete();
        BufferedWriter bw;
        StringBuilder sb = new StringBuilder();
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for(Map.Entry<String,Map<String,Object>> entry : db.entrySet()) {
                sb.append(entry.getKey());
                sb.append("#");
                for(Map.Entry<String,Object> subentry : entry.getValue().entrySet()) {
                    sb.append(subentry.getKey());
                    sb.append("&");
                    sb.append(subentry.getValue());
                    sb.append("$");
                }
                sb.delete(sb.length()-1, sb.length()); //delete $ from end of line
                sb.append("\r\n");
            }
            bw.write(sb.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
