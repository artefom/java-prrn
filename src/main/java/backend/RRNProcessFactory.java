package backend;

public class RRNProcessFactory implements ProcessFactoryBase {


    @Override
    public RRNProcessBase create_process() {
        return new RRNProcess();
    }

}
