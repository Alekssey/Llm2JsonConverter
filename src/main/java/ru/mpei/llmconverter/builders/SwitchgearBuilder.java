package ru.mpei.llmconverter.builders;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mpei.llmconverter.builders.model.SwitchgearBuilderOutDto;
import ru.mpei.llmconverter.builders.utils.BusConnectorMode;
import ru.mpei.llmconverter.builders.utils.ConnectionCreatorMode;
import ru.mpei.llmconverter.model.Link;
import ru.mpei.llmconverter.model.Node;
import ru.mpei.llmconverter.model.Scheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SwitchgearBuilder {
    @Autowired
    private ElementsBuilder elBuilder;
    @Autowired
    private LinkBuilder linker;
    @Getter @Setter
    private double refX = 0;
    @Getter @Setter
    private double refY = 0;

    public List<SwitchgearBuilderOutDto> buildQuadrilateral(Scheme scheme, Map<String, String> parameters) {
        this.refY = -950;
        if (this.refX != 0) this.refX += 300;
        String voltageClass = parameters.get("class").replace("kV", "");
        Node bus1 = this.elBuilder.buildBus(voltageClass, this.refX - 185, this.refY);
        Node bus2 = this.elBuilder.buildBus(voltageClass, this.refX - 185, this.refY + 590);
        scheme.getNodes().putAll(Map.of(bus1.getId(), bus1, bus2.getId(), bus2));
        double xCoord = -150;
        for (int i = 0; i < 2; i++) {
            double busRefCoordX = this.refX + xCoord - bus1.getCoords().getX();
            xCoord -= 31;
            Node dis1 = this.elBuilder.buildDisconnector(voltageClass, this.refX + xCoord, this.refY + 57.5);
            Node br1 = this.elBuilder.buildBreaker(voltageClass, this.refX + xCoord, refY + 145);
            Node dis2 = this.elBuilder.buildDisconnector(voltageClass, this.refX + xCoord, this.refY + 232.5);
            Node dis3 = this.elBuilder.buildDisconnector(voltageClass, this.refX + xCoord, this.refY + 347.5);
            Node br2 = this.elBuilder.buildBreaker(voltageClass, this.refX + xCoord, refY + 435);
            Node dis4 = this.elBuilder.buildDisconnector(voltageClass, this.refX + xCoord, this.refY + 522.5);

            Link link1 = this.linker.connectToBus(bus1, dis1, dis1.getPorts().get(0), BusConnectorMode.BUS_TO_EL, busRefCoordX, 3.5);
            Link link2 = this.linker.buildLink(dis1, dis1.getPorts().get(1), br1, br1.getPorts().get(0));
            Link link3 = this.linker.buildLink(br1, br1.getPorts().get(1), dis2, dis2.getPorts().get(0));
            Link link4 = this.linker.buildLink(dis2, dis2.getPorts().get(1), dis3, dis3.getPorts().get(0));
            Link link5 = this.linker.buildLink(dis3, dis3.getPorts().get(1), br2, br2.getPorts().get(0));
            Link link6 = this.linker.buildLink(br2, br2.getPorts().get(1), dis4, dis4.getPorts().get(0));
            Link link7 = this.linker.connectToBus(bus2, dis4, dis4.getPorts().get(1), BusConnectorMode.EL_TO_BUS, busRefCoordX, 3.5);

            scheme.getNodes().putAll(Map.of(dis1.getId(), dis1, dis2.getId(), dis2, dis3.getId(), dis3, dis4.getId(), dis4, br1.getId(), br1, br2.getId(), br2));
            scheme.getLinks().putAll(Map.of(link1.getId(), link1, link2.getId(), link2, link3.getId(), link3, link4.getId(), link4, link5.getId(), link5, link6.getId(), link6, link7.getId(), link7));
            xCoord = 150;
        }

        Node outDis1 = this.elBuilder.buildDisconnector(voltageClass, this.refX - 130, this.refY + 700);
        Node outDis2 = this.elBuilder.buildDisconnector(voltageClass, this.refX + 70, this.refY + 700);

        Link outLink1 = this.linker.connectToBus(bus1, outDis1, outDis1.getPorts().get(0), BusConnectorMode.BUS_TO_EL, this.refX - 100 - bus1.getCoords().getX(), 3.5);
        Link outLink2 = this.linker.connectToBus(bus2, outDis2, outDis2.getPorts().get(0), BusConnectorMode.BUS_TO_EL, this.refX + 100 - bus1.getCoords().getX(), 3.5);

        scheme.getNodes().putAll(Map.of(outDis1.getId(), outDis1, outDis2.getId(), outDis2));
        scheme.getLinks().putAll(Map.of(outLink1.getId(), outLink1, outLink2.getId(), outLink2));

        this.refX += 300;

        return List.of(new SwitchgearBuilderOutDto(outDis1, outDis1.getPorts().get(1)), new SwitchgearBuilderOutDto(outDis2, outDis2.getPorts().get(1)));
    }

    public List<SwitchgearBuilderOutDto> buildPartitionedBusBarSystem(Scheme scheme, Map<String, String> parameters) {
        this.refY = 700;
        double tempRefX = this.refX;
        this.refX = 0;

        String voltageClass = parameters.get("class").replace("kV", "");
        int numberOfConnections = Integer.parseInt(parameters.get("connections")) - 2;

        double step = 100;
        double busWidth = 370;
        if ((double) (numberOfConnections / 2 + numberOfConnections % 2 - 1) * step > busWidth) {
            busWidth = (numberOfConnections / 2 + numberOfConnections % 2 - 1) * step;
        } else if (numberOfConnections / 2 + numberOfConnections % 2 - 1 != 0) {
            step = busWidth / (numberOfConnections / 2 + numberOfConnections % 2 - 1);
        }
        step -= 5;

        if (this.refX != 0) this.refX += busWidth + 500;

        Node bus1 = this.elBuilder.buildBus(voltageClass, this.refX - 130 - busWidth , this.refY, busWidth);
        Node bus2 = this.elBuilder.buildBus(voltageClass, this.refX + 130, this.refY, busWidth);
        scheme.getNodes().putAll(Map.of(bus1.getId(), bus1, bus2.getId(), bus2));

        List<SwitchgearBuilderOutDto> sectionChain = buildConnection(scheme, this.refX - 220, this.refY, voltageClass, ConnectionCreatorMode.HORIZONTAL);
        Link sectionLink1 = this.linker.connectToBus(bus1, sectionChain.get(0).getNode(), sectionChain.get(0).getPort(), BusConnectorMode.BUS_TO_EL, bus1.getDimensions().getWidth() - 10, 3.5);
        Link sectionLink2 = this.linker.connectToBus(bus2, sectionChain.get(1).getNode(), sectionChain.get(1).getPort(), BusConnectorMode.BUS_TO_EL, 5, 3.5);

        List<List<SwitchgearBuilderOutDto>> outChains = List.of(
                this.buildConnection(scheme, this.refX - 350, this.refY - 50, voltageClass, ConnectionCreatorMode.UP),
                this.buildConnection(scheme, this.refX + 280, this.refY - 50, voltageClass, ConnectionCreatorMode.UP)
        );

        Link outLink1 = this.linker.connectToBus(bus1, outChains.get(0).get(0).getNode(), outChains.get(0).get(0).getPort(), BusConnectorMode.BUS_TO_EL, bus1.getDimensions().getWidth() / 2, 3.5);
        Link outLink2 = this.linker.connectToBus(bus2, outChains.get(1).get(0).getNode(), outChains.get(1).get(0).getPort(), BusConnectorMode.BUS_TO_EL, bus2.getDimensions().getWidth() / 2, 3.5);

        scheme.getLinks().putAll(Map.of(sectionLink1.getId(), sectionLink1, sectionLink2.getId(), sectionLink2, outLink1.getId(), outLink1, outLink2.getId(), outLink2));

        int connectionsPerBus = numberOfConnections / 2 + numberOfConnections % 2;
        for (int i = 0; i < connectionsPerBus; i++) {
            List<SwitchgearBuilderOutDto> chain = this.buildConnection(scheme, bus1.getCoords().getX() + i * step - 30, bus1.getCoords().getY() + 50, voltageClass, ConnectionCreatorMode.DOWN);
            Link link = this.linker.connectToBus(bus1, chain.get(0).getNode(), chain.get(0).getPort(), BusConnectorMode.BUS_TO_EL, i * step, 3.5);
            scheme.getLinks().put(link.getId(), link);
        }

        connectionsPerBus = numberOfConnections - connectionsPerBus;
        for (int i = 0; i < connectionsPerBus; i++) {
            List<SwitchgearBuilderOutDto> chain = this.buildConnection(scheme, bus2.getCoords().getX() + i * step - 30, this.refY + 50, voltageClass, ConnectionCreatorMode.DOWN);
            Link link = this.linker.connectToBus(bus2, chain.get(0).getNode(), chain.get(0).getPort(), BusConnectorMode.BUS_TO_EL, i * step, 3.5);
            scheme.getLinks().put(link.getId(), link);
        }

//        if(this.refY == 0) this.refY += 1000;
//        else this.refX += busWidth + 500;
        this.refX = tempRefX;

        return List.of(
                new SwitchgearBuilderOutDto(outChains.get(0).get(1).getNode(), outChains.get(0).get(1).getPort()),
                new SwitchgearBuilderOutDto(outChains.get(1).get(1).getNode(), outChains.get(1).get(1).getPort()));
    }


    public List<SwitchgearBuilderOutDto> build3on2(Scheme scheme, Map<String, String> parameters, int numberOfTransformers) {
        String voltageClass = parameters.get("class").replace("kV", "");
        int numberOfConnections = Integer.parseInt(parameters.get("connections"));

        int numberOfFullChains = numberOfConnections / 2;
        int numberOfPartChains = numberOfConnections % 2;
        double step = 200;
        double busWidth = (numberOfFullChains + numberOfPartChains - 1) * step;
        step -= 5;

        this.refY = -1400;
        if (this.refX != 0) this.refX += busWidth / 2 + 300;

        Map<Integer, ConnectionNodesHolder> chainsHolder = new HashMap<>();


        Node bus1 = this.elBuilder.buildBus(voltageClass, this.refX - busWidth / 2, this.refY, busWidth);
        Node bus2 = this.elBuilder.buildBus(voltageClass, this.refX - busWidth / 2, this.refY + 1040, busWidth);
        scheme.getNodes().putAll(Map.of(bus1.getId(), bus1, bus2.getId(), bus2));
        for (int i = 0; i < numberOfFullChains; i++) {
            double x = this.refX - busWidth / 2 + i * step;
            Node connectivityNode1 = this.elBuilder.buildConnectivityNode(voltageClass, x + 30, this.refY + 380);
            Node connectivityNode2 = this.elBuilder.buildConnectivityNode(voltageClass, x + 30, this.refY + 730);

            List<SwitchgearBuilderOutDto> subChain1 = this.buildConnection(scheme, x, this.refY + 30, voltageClass, ConnectionCreatorMode.DOWN);
            List<SwitchgearBuilderOutDto> subChain2 = this.buildConnection(scheme, x, this.refY + 380, voltageClass, ConnectionCreatorMode.DOWN);
            List<SwitchgearBuilderOutDto> subChain3 = this.buildConnection(scheme, x, this.refY + 730, voltageClass, ConnectionCreatorMode.DOWN);

            Link link1 = this.linker.connectToBus(bus1, subChain1.get(0).getNode(), subChain1.get(0).getPort(), BusConnectorMode.BUS_TO_EL, i * step, 3.5);
            Link link2 = this.linker.buildLink(subChain1.get(1).getNode(), subChain1.get(1).getPort(), connectivityNode1, connectivityNode1.getPorts().get(0));
            Link link3 = this.linker.buildLink(connectivityNode1, connectivityNode1.getPorts().get(0), subChain2.get(0).getNode(), subChain2.get(0).getPort());
            Link link4 = this.linker.buildLink(subChain2.get(1).getNode(), subChain2.get(1).getPort(), connectivityNode2, connectivityNode2.getPorts().get(0));
            Link link5 = this.linker.buildLink(connectivityNode2, connectivityNode2.getPorts().get(0), subChain3.get(0).getNode(), subChain3.get(0).getPort());
            Link link6 = this.linker.connectToBus(bus2, subChain3.get(1).getNode(), subChain3.get(1).getPort(), BusConnectorMode.EL_TO_BUS, i * step, 3.5);

            scheme.getNodes().putAll(Map.of(connectivityNode1.getId(), connectivityNode1, connectivityNode2.getId(), connectivityNode2));
            scheme.getLinks().putAll(Map.of(link1.getId(), link1, link2.getId(), link2, link3.getId(), link3, link4.getId(), link4, link5.getId(), link5, link6.getId(), link6));

            chainsHolder.put(i, new ConnectionNodesHolder(connectivityNode1, null, connectivityNode2));
        }

        if(numberOfPartChains != 0) {
            double x = this.refX - busWidth / 2 + (numberOfFullChains) * step;
            Node connectivityNode1 = this.elBuilder.buildConnectivityNode(voltageClass, x + 30, this.refY + 730);
            List<SwitchgearBuilderOutDto> subChain1 = this.buildConnection(scheme, x, this.refY + 380, voltageClass, ConnectionCreatorMode.DOWN);
            List<SwitchgearBuilderOutDto> subChain2 = this.buildConnection(scheme, x, this.refY + 730, voltageClass, ConnectionCreatorMode.DOWN);

            Link link1 = this.linker.connectToBus(bus1, subChain1.get(0).getNode(), subChain1.get(0).getPort(), BusConnectorMode.BUS_TO_EL, bus1.getDimensions().getWidth() - 10, 3.5);
            Link link2 = this.linker.buildLink(subChain1.get(1).getNode(), subChain1.get(1).getPort(), connectivityNode1, connectivityNode1.getPorts().get(0));
            Link link3 = this.linker.buildLink(connectivityNode1, connectivityNode1.getPorts().get(0), subChain2.get(0).getNode(), subChain2.get(0).getPort());
            Link link4 = this.linker.connectToBus(bus2, subChain2.get(1).getNode(), subChain2.get(1).getPort(), BusConnectorMode.EL_TO_BUS, bus2.getDimensions().getWidth() - 10, 3.5);

            scheme.getNodes().putAll(Map.of(connectivityNode1.getId(), connectivityNode1));
            scheme.getLinks().putAll(Map.of(link1.getId(), link1, link2.getId(), link2, link3.getId(), link3, link4.getId(), link4));

            chainsHolder.put(numberOfFullChains, new ConnectionNodesHolder(connectivityNode1, null, connectivityNode1));
        }

        List<SwitchgearBuilderOutDto> outList = new ArrayList<>();

        for (int i = 0; i < numberOfTransformers; i++) {
            Node connectivityNode;
            if (i % 2 == 0 && chainsHolder.get(i).upNode != null) {
                connectivityNode = chainsHolder.get(i).upNode;
            } else {
                connectivityNode = chainsHolder.get(i).downNode;
            }
            Node dis = this.elBuilder.buildDisconnector(voltageClass, connectivityNode.getCoords().getX() + 70, this.refY + 1150);
            Link link = this.linker.buildLink(connectivityNode, connectivityNode.getPorts().get(0), dis, dis.getPorts().get(0));
            outList.add(new SwitchgearBuilderOutDto(dis, dis.getPorts().get(1)));
            scheme.getNodes().put(dis.getId(), dis);
            scheme.getLinks().put(link.getId(), link);
        }

//        if(this.refY == 0) this.refY += 1600;
//        else if (this.refX == 0) this.refX += busWidth / 2 + 300;
        this.refX += busWidth / 2 + 300;

        return outList;
    }

    public List<SwitchgearBuilderOutDto> build4on3(Scheme scheme, Map<String, String> parameters, int numberOfTransformers) {
        String voltageClass = parameters.get("class").replace("kV", "");
        int numberOfConnections = Integer.parseInt(parameters.get("connections"));

        int numberOfFullChains = numberOfConnections / 3;
        int numberOfPartChains = numberOfConnections % 3 == 0 ? 0: 1;
        double step = 200;
        double busWidth = (numberOfFullChains + numberOfPartChains - 1) * step;
        step -= 5;

        this.refY = -1750;
        if (this.refX != 0) this.refX += busWidth / 2 + 300;

        Map<Integer, ConnectionNodesHolder> chainsHolder = new HashMap<>();

        Node bus1 = this.elBuilder.buildBus(voltageClass, this.refX - busWidth / 2, this.refY, busWidth);
        Node bus2 = this.elBuilder.buildBus(voltageClass, this.refX - busWidth / 2, this.refY + 1390, busWidth);
        scheme.getNodes().putAll(Map.of(bus1.getId(), bus1, bus2.getId(), bus2));
        for (int i = 0; i < numberOfFullChains; i++) {
            double x = this.refX - busWidth / 2 + i * step;
            Node connectivityNode1 = this.elBuilder.buildConnectivityNode(voltageClass, x + 30, this.refY + 380);
            Node connectivityNode2 = this.elBuilder.buildConnectivityNode(voltageClass, x + 30, this.refY + 730);
            Node connectivityNode3 = this.elBuilder.buildConnectivityNode(voltageClass, x + 30, this.refY + 1080);

            List<SwitchgearBuilderOutDto> subChain1 = this.buildConnection(scheme, x, this.refY + 30, voltageClass, ConnectionCreatorMode.DOWN);
            List<SwitchgearBuilderOutDto> subChain2 = this.buildConnection(scheme, x, this.refY + 380, voltageClass, ConnectionCreatorMode.DOWN);
            List<SwitchgearBuilderOutDto> subChain3 = this.buildConnection(scheme, x, this.refY + 730, voltageClass, ConnectionCreatorMode.DOWN);
            List<SwitchgearBuilderOutDto> subChain4 = this.buildConnection(scheme, x, this.refY + 1080, voltageClass, ConnectionCreatorMode.DOWN);

            Link link1 = this.linker.connectToBus(bus1, subChain1.get(0).getNode(), subChain1.get(0).getPort(), BusConnectorMode.BUS_TO_EL, i * step, 3.5);
            Link link2 = this.linker.buildLink(subChain1.get(1).getNode(), subChain1.get(1).getPort(), connectivityNode1, connectivityNode1.getPorts().get(0));
            Link link3 = this.linker.buildLink(connectivityNode1, connectivityNode1.getPorts().get(0), subChain2.get(0).getNode(), subChain2.get(0).getPort());
            Link link4 = this.linker.buildLink(subChain2.get(1).getNode(), subChain2.get(1).getPort(), connectivityNode2, connectivityNode2.getPorts().get(0));
            Link link5 = this.linker.buildLink(connectivityNode2, connectivityNode2.getPorts().get(0), subChain3.get(0).getNode(), subChain3.get(0).getPort());
            Link link6 = this.linker.buildLink(subChain3.get(1).getNode(), subChain3.get(1).getPort(), connectivityNode3, connectivityNode3.getPorts().get(0));
            Link link7 = this.linker.buildLink(connectivityNode3, connectivityNode3.getPorts().get(0), subChain4.get(0).getNode(), subChain4.get(0).getPort());
            Link link8 = this.linker.connectToBus(bus2, subChain4.get(1).getNode(), subChain4.get(1).getPort(), BusConnectorMode.EL_TO_BUS, i * step, 3.5);

            scheme.getNodes().putAll(Map.of(connectivityNode1.getId(), connectivityNode1, connectivityNode2.getId(), connectivityNode2, connectivityNode3.getId(), connectivityNode3));
            scheme.getLinks().putAll(Map.of(link1.getId(), link1, link2.getId(), link2, link3.getId(), link3, link4.getId(), link4, link5.getId(), link5, link6.getId(), link6, link7.getId(), link7, link8.getId(), link8));

            chainsHolder.put(i, new ConnectionNodesHolder(connectivityNode1, connectivityNode2, connectivityNode3));
        }

        if(numberOfPartChains != 0) {
            double x = this.refX - busWidth / 2 + numberOfFullChains * step;
            double y = this.refY + 30;
            List<SwitchgearBuilderOutDto> firstSubChain = null, lastSubChain = null;
            List<Node> connectivityNodeList = new ArrayList<>();
            for (int i = 0; i < numberOfConnections % 3 + 1; i++) {
                List<SwitchgearBuilderOutDto> newSubChain = this.buildConnection(scheme, x, y, voltageClass, ConnectionCreatorMode.DOWN);
                if (firstSubChain == null) firstSubChain = newSubChain;
                if (lastSubChain == null) {
                    lastSubChain = newSubChain;
                } else {
                    Node connectivityNode = this.elBuilder.buildConnectivityNode(voltageClass, lastSubChain.get(1).getNode().getPorts().get(1).getCoords().getX(), lastSubChain.get(1).getNode().getPorts().get(1).getCoords().getY() + 45);
                    connectivityNodeList.add(connectivityNode);
                    Link link1 = this.linker.buildLink(lastSubChain.get(1).getNode(), lastSubChain.get(1).getPort(), connectivityNode, connectivityNode.getPorts().get(0));
                    Link link2 = this.linker.buildLink(connectivityNode, connectivityNode.getPorts().get(0), newSubChain.get(0).getNode(), newSubChain.get(0).getPort());
                    scheme.getNodes().put(connectivityNode.getId(), connectivityNode);
                    scheme.getLinks().putAll(Map.of(link1.getId(), link1, link2.getId(), link2));
                    lastSubChain = newSubChain;
                }
                y += 350;
            }
            Link busConnection1 = this.linker.connectToBus(bus1, firstSubChain.get(0).getNode(), firstSubChain.get(0).getPort(), BusConnectorMode.BUS_TO_EL, bus1.getDimensions().getWidth() - 10, 3.5);
            Link busConnection2 = this.linker.connectToBus(bus2, lastSubChain.get(1).getNode(), lastSubChain.get(1).getPort(), BusConnectorMode.BUS_TO_EL, bus1.getDimensions().getWidth() - 10, 3.5);

            scheme.getLinks().putAll(Map.of(busConnection1.getId(), busConnection1, busConnection2.getId(), busConnection2));
            chainsHolder.put(chainsHolder.size(), new ConnectionNodesHolder(
                    connectivityNodeList.get(0),
                    connectivityNodeList.size() >= 2 ? connectivityNodeList.get(1): null,
                    null));
        }

        List<SwitchgearBuilderOutDto> outList = new ArrayList<>();

        while (numberOfTransformers > 0) {
            for (int index: chainsHolder.keySet()) {
                if (chainsHolder.get(index).upNode.getPorts().get(0).getLinks().size() < 3) {
                    Node dis = this.elBuilder.buildDisconnector(voltageClass, chainsHolder.get(index).upNode.getCoords().getX() + 40, this.refY + 1500);
                    Link link = this.linker.buildLink(chainsHolder.get(index).upNode, chainsHolder.get(index).upNode.getPorts().get(0), dis, dis.getPorts().get(0));
                    outList.add(new SwitchgearBuilderOutDto(dis, dis.getPorts().get(1)));
                    scheme.getNodes().put(dis.getId(), dis);
                    scheme.getLinks().put(link.getId(), link);
                    numberOfTransformers --;
                    break;
                } else if (chainsHolder.get(index).downNode.getPorts().get(0).getLinks().size() < 3) {
                    Node dis = this.elBuilder.buildDisconnector(voltageClass, chainsHolder.get(index).downNode.getCoords().getX() + 80, this.refY + 1500);
                    Link link = this.linker.buildLink(chainsHolder.get(index).downNode, chainsHolder.get(index).downNode.getPorts().get(0), dis, dis.getPorts().get(0));
                    outList.add(new SwitchgearBuilderOutDto(dis, dis.getPorts().get(1)));
                    scheme.getNodes().put(dis.getId(), dis);
                    scheme.getLinks().put(link.getId(), link);
                    numberOfTransformers --;
                    break;
                }
            }
        }

//        if(this.refY == 0) this.refY += 1600;
//        else if (this.refX == 0) this.refX += busWidth / 2 + 300;
        this.refX += busWidth / 2 + 300;

        return outList;
    }

    public List<SwitchgearBuilderOutDto> buildHexagonal(Scheme scheme, Map<String, String> parameters) {
        this.refY = -700;
        if (this.refX != 0) this.refX += 400;

        String voltageClass = parameters.get("class").replace("kV", "");
        Map<Integer, Node> connectivityNodes = Map.of(
                0, this.elBuilder.buildConnectivityNode(voltageClass, this.refX - 350, this.refY),
                1, this.elBuilder.buildConnectivityNode(voltageClass, this.refX, this.refY),
                2, this.elBuilder.buildConnectivityNode(voltageClass, this.refX + 350, this.refY),
                3, this.elBuilder.buildConnectivityNode(voltageClass, this.refX + 350, this.refY + 350),
                4, this.elBuilder.buildConnectivityNode(voltageClass, this.refX, this.refY + 350),
                5, this.elBuilder.buildConnectivityNode(voltageClass, this.refX - 350, this.refY + 350)
        );

        Map<Integer, List<SwitchgearBuilderOutDto>> subChains = Map.of(
                0, this.buildConnection(scheme, this.refX - 395, this.refY, voltageClass, ConnectionCreatorMode.HORIZONTAL),
                1, this.buildConnection(scheme, this.refX - 45, this.refY, voltageClass, ConnectionCreatorMode.HORIZONTAL),
                2, this.buildConnection(scheme, this.refX + 350, this.refY + 30, voltageClass, ConnectionCreatorMode.DOWN),
                3, this.buildConnection(scheme, this.refX - 45, this.refY + 350, voltageClass, ConnectionCreatorMode.HORIZONTAL),
                4, this.buildConnection(scheme, this.refX - 395, this.refY + 350, voltageClass, ConnectionCreatorMode.HORIZONTAL),
                5, this.buildConnection(scheme, this.refX - 350, this.refY + 320, voltageClass, ConnectionCreatorMode.UP)
        );

        List<SwitchgearBuilderOutDto> outList = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            if (i <= 2) {
                Link backLink = this.linker.buildLink(connectivityNodes.get(i), connectivityNodes.get(i).getPorts().get(0), subChains.get(i).get(0).getNode(), subChains.get(i).get(0).getPort());
                Link forwardLink = this.linker.buildLink(subChains.get(i).get(1).getNode(), subChains.get(i).get(1).getPort(), connectivityNodes.get(i + 1), connectivityNodes.get(i + 1).getPorts().get(0));
                scheme.getNodes().put(connectivityNodes.get(i).getId(), connectivityNodes.get(i));
                scheme.getLinks().putAll(Map.of(backLink.getId(), backLink, forwardLink.getId(), forwardLink));
            } else if (i <= 4) {
                Link backLink = this.linker.buildLink(connectivityNodes.get(i), connectivityNodes.get(i).getPorts().get(0), subChains.get(i).get(1).getNode(), subChains.get(i).get(1).getPort());
                Link forwardLink = this.linker.buildLink(subChains.get(i).get(0).getNode(), subChains.get(i).get(0).getPort(), connectivityNodes.get(i + 1), connectivityNodes.get(i + 1).getPorts().get(0));
                scheme.getNodes().put(connectivityNodes.get(i).getId(), connectivityNodes.get(i));
                scheme.getLinks().putAll(Map.of(backLink.getId(), backLink, forwardLink.getId(), forwardLink));
            } else {
                Link backLink = this.linker.buildLink(connectivityNodes.get(i), connectivityNodes.get(i).getPorts().get(0), subChains.get(i).get(0).getNode(), subChains.get(i).get(0).getPort());
                Link forwardLink = this.linker.buildLink(subChains.get(i).get(1).getNode(), subChains.get(i).get(1).getPort(), connectivityNodes.get(0), connectivityNodes.get(0).getPorts().get(0));
                scheme.getNodes().put(connectivityNodes.get(i).getId(), connectivityNodes.get(i));
                scheme.getLinks().putAll(Map.of(backLink.getId(), backLink, forwardLink.getId(), forwardLink));
            }

            if (i == 3 || i == 5) {
                Node connectivityNode = connectivityNodes.get(i);
                Node dis = this.elBuilder.buildDisconnector(voltageClass, connectivityNode.getPorts().get(0).getCoords().getX(), connectivityNode.getPorts().get(0).getCoords().getY() + 100);
                Link link = this.linker.buildLink(connectivityNode, connectivityNode.getPorts().get(0), dis, dis.getPorts().get(0));
                outList.add(new SwitchgearBuilderOutDto(dis, dis.getPorts().get(1)));
                scheme.getNodes().put(dis.getId(), dis);
                scheme.getLinks().put(link.getId(), link);
            }
        }
//        if(this.refY == 0) this.refY += 1600;
//        else if (this.refX == 0) this.refX += 400;
        this.refX += 400;

        return outList;

    }

    private List<SwitchgearBuilderOutDto> buildConnection(Scheme scheme, double x, double y, String voltageClass, ConnectionCreatorMode direction) {
        List<SwitchgearBuilderOutDto> outList;
        Link link1;
        Link link2;
        Node dis1;
        Node br1;
        Node dis2;
        if (!direction.equals(ConnectionCreatorMode.HORIZONTAL)) {
            dis1 = this.elBuilder.buildDisconnector(voltageClass, x, direction.equals(ConnectionCreatorMode.DOWN) ? (y + 57.5) : (y - 57.5));
            br1 = this.elBuilder.buildBreaker(voltageClass, x, direction.equals(ConnectionCreatorMode.DOWN) ? (y + 145) : (y - 145));
            dis2 = this.elBuilder.buildDisconnector(voltageClass, x, direction.equals(ConnectionCreatorMode.DOWN) ? (y + 232.5) : (y - 232.5));
            if (direction.equals(ConnectionCreatorMode.DOWN)) {
                link1 = this.linker.buildLink(dis1, dis1.getPorts().get(1), br1, br1.getPorts().get(0));
                link2 = this.linker.buildLink(br1, br1.getPorts().get(1), dis2, dis2.getPorts().get(0));
                outList = List.of(new SwitchgearBuilderOutDto(dis1, dis1.getPorts().get(0)), new SwitchgearBuilderOutDto(dis2, dis2.getPorts().get(1)));
            } else {
                link1 = this.linker.buildLink(dis1, dis1.getPorts().get(0), br1, br1.getPorts().get(1));
                link2 = this.linker.buildLink(br1, br1.getPorts().get(0), dis2, dis2.getPorts().get(1));
                outList = List.of(new SwitchgearBuilderOutDto(dis1, dis1.getPorts().get(1)), new SwitchgearBuilderOutDto(dis2, dis2.getPorts().get(0)));
            }
        } else {
            dis1 = this.elBuilder.buildDisconnector(voltageClass, x + 57.5, y, 1);
            br1 = this.elBuilder.buildBreaker(voltageClass, x + 145, y, 1);
            dis2 = this.elBuilder.buildDisconnector(voltageClass, x + 232.5, y, 1);

            link1 = this.linker.buildLink(dis1, dis1.getPorts().get(0), br1, br1.getPorts().get(1));
            link2 = this.linker.buildLink(br1, br1.getPorts().get(0), dis2, dis2.getPorts().get(1));
            outList = List.of(new SwitchgearBuilderOutDto(dis1, dis1.getPorts().get(1)), new SwitchgearBuilderOutDto(dis2, dis2.getPorts().get(0)));
        }

        scheme.getNodes().putAll(Map.of(dis1.getId(), dis1, dis2.getId(), dis2, br1.getId(), br1));
        scheme.getLinks().putAll(Map.of(link1.getId(), link1, link2.getId(), link2));

        return outList;
    }

    private record ConnectionNodesHolder(Node upNode, Node midNode, Node downNode){}

    public void reset() {
        this.refX = 0;
        this.refY = 0;
    }

}
