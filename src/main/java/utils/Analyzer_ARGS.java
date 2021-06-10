package utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Analyzer_ARGS {

    private  String settingPath = null;
    private  String otziv = null;


    public Analyzer_ARGS(String[] args) {
        if(args.length >= 2){
            for(int i = 0 ; i < args.length; i++){
                if("-s".equals(args[i])){
                    settingPath = args[i + 1];
                }
                if("-a".equals(args[i])){
                    otziv = args[i + 1];
                }
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
