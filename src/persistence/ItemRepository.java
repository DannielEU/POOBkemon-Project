package persistence;

import domain.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ItemRepository {

    private static final String ITEMS_ARCHIVE ="resources/csv/Items.csv";
    private static ArrayList<ArrayList<String>> items = new ArrayList<>();
    
    public ItemRepository(){
    	ArrayList<ArrayList<String>> items = new ArrayList<>();
        try {
			List<String> lineas = Files.readAllLines(Paths.get(ITEMS_ARCHIVE));

		    for (String s : lineas) {
		        String[] valores = s.split(",");
		        ArrayList<String> informacion = new ArrayList<>();
		        informacion.add(valores[0]);
		        informacion.add(valores[1]);
		        items.add(informacion);
		        
		    }
		} catch (IOException e) {
			Log.record(e);
		}
        this.items = items;
    	
    }

    public ArrayList<ArrayList<String>> getItems() {
        return this.items;
    }
}
