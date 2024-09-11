package ru.mpei.llmconverter.builders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mpei.llmconverter.builders.utils.BusConnectorMode;
import ru.mpei.llmconverter.model.Link;
import ru.mpei.llmconverter.model.Node;
import ru.mpei.llmconverter.model.Port;
import ru.mpei.llmconverter.utils.IdGenerator;

import java.util.List;

@Component
public class LinkBuilder {
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private ElementsBuilder elBuilder;
    public Link buildLink(Node srcNode, Port srcPort, Node dstNode, Port dstPort) {
        Link link = new Link(
                this.idGenerator.generateId(),
                "RECTANGULAR",
                false,
                false,
                srcNode.getId(),
                dstNode.getId(),
                srcPort.getId(),
                dstPort.getId(),
                List.of(
                        this.elBuilder.buildPoint(srcPort.getCoords().getX(), srcPort.getCoords().getY()),
                        this.elBuilder.buildPoint(dstPort.getCoords().getX(), dstPort.getCoords().getY()))
        );
        srcPort.getLinks().add(link.getId());
        dstPort.getLinks().add(link.getId());
        return link;
    }

    public Link connectToBus(Node bus, Node connectingEl, Port connectingElPort, BusConnectorMode mode, double x, double y) {
        Port newPortForBus = this.elBuilder.buildPort(bus.getId(), "NO_ID", "BOTTOM", x, y);
        bus.getPorts().add(newPortForBus);
        Link link;
        if(mode.equals(BusConnectorMode.BUS_TO_EL)){
            link = this.buildLink(bus, newPortForBus, connectingEl, connectingElPort);
        } else {
            link = this.buildLink(connectingEl, connectingElPort, bus, newPortForBus);
        }
//        newPortForBus.getCoords().setX(x);
//        newPortForBus.getCoords().setY(y);
//        newPortForBus.getLinks().add(link.getId());
//        connectingElPort.getLinks().add(link.getId());
        return link;
    }
}
