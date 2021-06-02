package dict.CorrectVertexWeight;

import dict.DictBase;
import dict.DictException;

import java.io.IOException;

public interface CorrectVertexWeightInterface {
    void correctVertexWeight(DictBase dictBase) throws DictException, InterruptedException, IOException, IllegalAccessException;
}
