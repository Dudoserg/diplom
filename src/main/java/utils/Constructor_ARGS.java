package utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Constructor_ARGS {

    private  String settingPath = null;


    public Constructor_ARGS(String[] args) {
        if(args.length == 2){
            if("-s".equals(args[0])){
                settingPath = args[1];
            }
        }
    }


    public String getSetting(){
        if(settingPath != null){
            return settingPath;
        }
        return null;
    }
}
