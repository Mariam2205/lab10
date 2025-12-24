


public class Catalog {
    public boolean current; 
    public boolean allModesExist; 
    
    public Catalog(boolean current, boolean allModesExist) {
        this.current = current;
        this.allModesExist = allModesExist;
    }
    
    public boolean hasCurrent() {
        return current;
    }
    
    public boolean hasAllModes() {
        return allModesExist;
    }
}
