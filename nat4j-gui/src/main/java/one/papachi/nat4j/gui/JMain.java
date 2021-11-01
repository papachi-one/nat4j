/*
 * Created by JFormDesigner on Fri Sep 10 19:22:58 CEST 2021
 */

package one.papachi.nat4j.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import one.papachi.nat4j.lib.NatUtils;
import one.papachi.nat4j.lib.portmapper.MappedPort;
import one.papachi.nat4j.lib.portmapper.PortMapper;
import one.papachi.nat4j.lib.portmapper.PortProtocol;
import one.papachi.nat4j.lib.portmapper.natpmp.NatPmpPortMapper;
import one.papachi.nat4j.lib.portmapper.pcp.PCPPortMapper;
import one.papachi.nat4j.lib.portmapper.upnp.UPnPPortMapper;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * @author Pavel Csefalvay
 */
public class JMain extends JFrame {

    private List<PortMapper> ipv4PortMappers, ipv6PortMappers;

    private List<MappedPort> mappedPorts = new LinkedList<>();

    public JMain() {
        initComponents();
    }

    private void appendLog(String s) {
        try {
            SwingUtilities.invokeAndWait(() -> textPaneLog.setText(textPaneLog.getText() + s));
        } catch (Exception e) {
        }
    }

    private void setStatus(String s) {
        try {
            SwingUtilities.invokeAndWait(() -> labelStatus.setText(s));
        } catch (Exception e) {
        }
    }

    AbstractTableModel model = new AbstractTableModel() {

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> PortProtocol.class;
                case 1, 2 -> Integer.class;
                case 3 -> String.class;
                default -> Objects.class;
            };
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "Protocol";
                case 1 -> "Internal Port";
                case 2 -> "External Port";
                case 3 -> "Status";
                default -> "Unknown";
            };
        }

        @Override
        public int getRowCount() {
            return mappedPorts.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MappedPort mappedPort = mappedPorts.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> mappedPort.getPortProtocol();
                case 1 -> mappedPort.getInternalPort();
                case 2 -> mappedPort.getExternalPort();
                default -> null;
            };
        }
    };

    public void init() {
        tableMappings.setModel(model);
        try {
            setStatus("Detecting local IPv4 address...");
            appendLog("Detecting local IPv4 address...");
            Inet4Address localAddressToDefaultGatewayIPv4 = NatUtils.getLocalAddressToDefaultGatewayIPv4();
            SwingUtilities.invokeAndWait(() -> textFieldLocalIPv4.setText(localAddressToDefaultGatewayIPv4 != null ? localAddressToDefaultGatewayIPv4.getHostAddress() : "-"));
            appendLog((localAddressToDefaultGatewayIPv4 != null ? localAddressToDefaultGatewayIPv4.getHostAddress() : "-") + "\n");

            setStatus("Detecting default IPv4 gateway address...");
            appendLog("Detecting default IPv4 gateway address...");
            Inet4Address defaultGatewayIPv4 = NatUtils.getDefaultGatewayIPv4();
            SwingUtilities.invokeAndWait(() -> textFieldGatewayIPv4.setText(defaultGatewayIPv4 != null ? defaultGatewayIPv4.getHostAddress() : "-"));
            appendLog((defaultGatewayIPv4 != null ? defaultGatewayIPv4.getHostAddress() : "-") + "\n");

            setStatus("Detecting local IPv6 addresses...");
            appendLog("Detecting local IPv6 addresses...");
            List<Inet6Address> localAddressesToDefaultGatewayIPv6 = NatUtils.getLocalAddressesToDefaultGatewayIPv6();
            SwingUtilities.invokeAndWait(() -> textFieldLocalIPv6.setText(!localAddressesToDefaultGatewayIPv6.isEmpty() ? localAddressesToDefaultGatewayIPv6.stream().map(InetAddress::getHostAddress).collect(Collectors.joining(";")) : "-"));
            appendLog((!localAddressesToDefaultGatewayIPv6.isEmpty() ? localAddressesToDefaultGatewayIPv6.stream().map(InetAddress::getHostAddress).collect(Collectors.joining("; ")) : "-") + "\n");

            setStatus("Detecting default IPv6 gateway address...");
            appendLog("Detecting default IPv6 gateway address...");
            Inet6Address defaultGatewayIPv6 = NatUtils.getDefaultGatewayIPv6();
            SwingUtilities.invokeAndWait(() -> textFieldGatewayIPv6.setText(defaultGatewayIPv6 != null ? defaultGatewayIPv6.getHostAddress() : "-"));
            appendLog((defaultGatewayIPv6 != null ? defaultGatewayIPv6.getHostAddress() : "-") + "\n");

            setStatus("Detecting NAT-PMP enabled gateways...");
            appendLog("Detecting NAT-PMP enabled gateways...");
            List<PortMapper> natPmpPortMappers = NatPmpPortMapper.find();
            appendLog("Found: " + natPmpPortMappers.size() + "\n");

            setStatus("Detecting UPnP-IGD enabled gateways...");
            appendLog("Detecting UPnP-IGD enabled gateways...");
            List<PortMapper> uPnpPortMappers = UPnPPortMapper.find();
            appendLog("Found: " + uPnpPortMappers.size() + "\n");

            setStatus("Detecting PCP enabled gateways...");
            appendLog("Detecting PCP enabled gateways...");
            List<PortMapper> pcpPortMappers = PCPPortMapper.find();
            appendLog("Found: " + pcpPortMappers.size() + "\n");

            setStatus("Ready");

            SwingUtilities.invokeAndWait(() -> textFieldPublicIPv4.setText("-"));
            SwingUtilities.invokeAndWait(() -> textFieldPublicIPv6.setText("-"));
            SwingUtilities.invokeAndWait(() -> natPmpPortMappers.stream().map(PortMapper::getExternalAddress).filter(Objects::nonNull).filter(a -> !a.getHostAddress().equals("0.0.0.0")).findAny().ifPresent(externalAddress -> textFieldPublicIPv4.setText(externalAddress.getHostAddress())));
            SwingUtilities.invokeAndWait(() -> uPnpPortMappers.stream().map(PortMapper::getExternalAddress).filter(Objects::nonNull).filter(a -> !a.getHostAddress().equals("0.0.0.0")).filter(a -> a.getAddress().length == 4).findAny().ifPresent(externalAddress -> textFieldPublicIPv4.setText(externalAddress.getHostAddress())));
            SwingUtilities.invokeAndWait(() -> uPnpPortMappers.stream().map(PortMapper::getExternalAddress).filter(Objects::nonNull).filter(a -> a.getAddress().length == 16).findAny().ifPresent(externalAddress -> textFieldPublicIPv6.setText(externalAddress.getHostAddress())));
            SwingUtilities.invokeAndWait(() -> pcpPortMappers.stream().map(PortMapper::getExternalAddress).filter(Objects::nonNull).filter(a -> !a.getHostAddress().equals("0.0.0.0")).filter(a -> a.getAddress().length == 4).findAny().ifPresent(externalAddress -> textFieldPublicIPv4.setText(externalAddress.getHostAddress())));
            SwingUtilities.invokeAndWait(() -> pcpPortMappers.stream().map(PortMapper::getExternalAddress).filter(Objects::nonNull).filter(a -> a.getAddress().length == 16).findAny().ifPresent(externalAddress -> textFieldPublicIPv6.setText(externalAddress.getHostAddress())));

            ipv4PortMappers = List.of(natPmpPortMappers, uPnpPortMappers, pcpPortMappers).stream().flatMap(Collection::stream).filter(pm -> pm.getGatewayAddress().getAddress().length == 4).toList();
            ipv6PortMappers = List.of(natPmpPortMappers, uPnpPortMappers, pcpPortMappers).stream().flatMap(Collection::stream).filter(pm -> pm.getGatewayAddress().getAddress().length == 16).toList();

            if (!natPmpPortMappers.isEmpty() || !uPnpPortMappers.isEmpty() || !pcpPortMappers.isEmpty()) {
                SwingUtilities.invokeAndWait(() -> buttonActionAdd.setEnabled(true));
            }

        } catch (Exception e) {

        }
    }

    private void labelLinkMouseClicked(MouseEvent e) {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/papachi-one/nat4j"));
        } catch (URISyntaxException | IOException ex) {
        }
    }

    private void buttonActionAddActionPerformed(ActionEvent e) {
        dialogAddPortMapping.pack();
        dialogAddPortMapping.setLocationRelativeTo(this);
        dialogAddPortMapping.setVisible(true);
    }

    private void buttonCancelActionPerformed(ActionEvent e) {
        dialogAddPortMapping.setVisible(false);
    }

    private void buttonSaveActionPerformed(ActionEvent e) {
        PortProtocol protocol = PortProtocol.valueOf((String) comboBoxProtocol.getSelectedItem());
        int internalPort = (Integer) spinnerInternalPort.getValue();
        int externalPort = (Integer) spinnerExternalPort.getValue();
        dialogAddPortMapping.setVisible(false);
        new Thread(() -> {
            try {
                map(protocol, internalPort, externalPort);
            } catch (Exception ex) {
            }
        }).start();
    }

    private void map(PortProtocol protocol, int internalPort, int externalPort) throws Exception {
        setStatus("Mapping " + externalPort + " -> " + internalPort + "(" + protocol + ")...");
        appendLog("Mapping " + externalPort + " -> " + internalPort + "(" + protocol + ")...");
        MappedPort mappedPort = ipv4PortMappers.stream().map(pm -> pm.mapPort(protocol, internalPort, externalPort, 300)).findFirst().get().orElse(null);
        if (mappedPort == null)
            mappedPort = ipv6PortMappers.stream().map(pm -> pm.mapPort(protocol, internalPort, externalPort, 300)).findFirst().get().orElse(null);
        appendLog(mappedPort != null ? "success" : "failed");
        appendLog("\n");
        mappedPorts.add(mappedPort);
        model.fireTableDataChanged();
        setStatus("Ready");
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        panelStatus = new JPanel();
        labelStatus = new JLabel();
        tabbedPane = new JTabbedPane();
        panelHome = new JPanel();
        panelHomeForm = new JPanel();
        labelIPv4 = new JLabel();
        labelIPv6 = new JLabel();
        labelLocalIP = new JLabel();
        textFieldLocalIPv4 = new JTextField();
        textFieldLocalIPv6 = new JTextField();
        labelGatewayIP = new JLabel();
        textFieldGatewayIPv4 = new JTextField();
        textFieldGatewayIPv6 = new JTextField();
        labelPublicIP = new JLabel();
        textFieldPublicIPv4 = new JTextField();
        textFieldPublicIPv6 = new JTextField();
        panelMappings = new JPanel();
        scrollPaneMappings = new JScrollPane();
        tableMappings = new JTable();
        panelMappingsActions = new JPanel();
        buttonActionAdd = new JButton();
        buttonActionRemove = new JButton();
        panelLog = new JPanel();
        scrollPaneLog = new JScrollPane();
        textPaneLog = new JTextPane();
        panelAbout = new JPanel();
        labelAbout = new JLabel();
        labelLink = new JLabel();
        dialogAddPortMapping = new JDialog();
        panelAddPortMapping = new JPanel();
        buttonSave = new JButton();
        buttonCancel = new JButton();
        panelAddPortMappingForm = new JPanel();
        labelProtocol = new JLabel();
        comboBoxProtocol = new JComboBox<>();
        labelInternalPort = new JLabel();
        spinnerInternalPort = new JSpinner();
        labelExternalPort = new JLabel();
        spinnerExternalPort = new JSpinner();

        setTitle("one.papachi.nat4j");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        {
            panelStatus.setBorder(new EmptyBorder(5, 5, 5, 5));
            panelStatus.setLayout(new BorderLayout());

            labelStatus.setText(" ");
            panelStatus.add(labelStatus, BorderLayout.CENTER);
        }
        contentPane.add(panelStatus, BorderLayout.SOUTH);

        {

            {
                panelHome.setLayout(new GridBagLayout());
                ((GridBagLayout)panelHome.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)panelHome.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)panelHome.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)panelHome.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

                {
                    panelHomeForm.setLayout(new GridBagLayout());
                    ((GridBagLayout)panelHomeForm.getLayout()).columnWidths = new int[] {0, 105, 200, 0};
                    ((GridBagLayout)panelHomeForm.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
                    ((GridBagLayout)panelHomeForm.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)panelHomeForm.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

                    labelIPv4.setText("IPv4");
                    labelIPv4.setHorizontalAlignment(SwingConstants.CENTER);
                    panelHomeForm.add(labelIPv4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    labelIPv6.setText("IPv6");
                    labelIPv6.setHorizontalAlignment(SwingConstants.CENTER);
                    panelHomeForm.add(labelIPv6, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    labelLocalIP.setText("Local IP");
                    labelLocalIP.setHorizontalAlignment(SwingConstants.TRAILING);
                    panelHomeForm.add(labelLocalIP, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    textFieldLocalIPv4.setEditable(false);
                    textFieldLocalIPv4.setHorizontalAlignment(SwingConstants.CENTER);
                    textFieldLocalIPv4.setText("?");
                    panelHomeForm.add(textFieldLocalIPv4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    textFieldLocalIPv6.setEditable(false);
                    textFieldLocalIPv6.setHorizontalAlignment(SwingConstants.CENTER);
                    textFieldLocalIPv6.setText("?");
                    panelHomeForm.add(textFieldLocalIPv6, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    labelGatewayIP.setText("Gateway IP");
                    labelGatewayIP.setHorizontalAlignment(SwingConstants.TRAILING);
                    panelHomeForm.add(labelGatewayIP, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    textFieldGatewayIPv4.setEditable(false);
                    textFieldGatewayIPv4.setHorizontalAlignment(SwingConstants.CENTER);
                    textFieldGatewayIPv4.setText("?");
                    panelHomeForm.add(textFieldGatewayIPv4, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    textFieldGatewayIPv6.setEditable(false);
                    textFieldGatewayIPv6.setHorizontalAlignment(SwingConstants.CENTER);
                    textFieldGatewayIPv6.setText("?");
                    panelHomeForm.add(textFieldGatewayIPv6, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    labelPublicIP.setText("Public IP");
                    labelPublicIP.setHorizontalAlignment(SwingConstants.TRAILING);
                    panelHomeForm.add(labelPublicIP, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    textFieldPublicIPv4.setEditable(false);
                    textFieldPublicIPv4.setHorizontalAlignment(SwingConstants.CENTER);
                    textFieldPublicIPv4.setText("?");
                    panelHomeForm.add(textFieldPublicIPv4, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    textFieldPublicIPv6.setEditable(false);
                    textFieldPublicIPv6.setHorizontalAlignment(SwingConstants.CENTER);
                    textFieldPublicIPv6.setText("?");
                    panelHomeForm.add(textFieldPublicIPv6, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                panelHome.add(panelHomeForm, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane.addTab("Home", panelHome);

            {
                panelMappings.setLayout(new BorderLayout());

                {

                    tableMappings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    scrollPaneMappings.setViewportView(tableMappings);
                }
                panelMappings.add(scrollPaneMappings, BorderLayout.CENTER);

                {
                    panelMappingsActions.setLayout(new FlowLayout(FlowLayout.RIGHT));

                    buttonActionAdd.setText("+");
                    buttonActionAdd.setEnabled(false);
                    buttonActionAdd.addActionListener(e -> buttonActionAddActionPerformed(e));
                    panelMappingsActions.add(buttonActionAdd);

                    buttonActionRemove.setText("-");
                    buttonActionRemove.setEnabled(false);
                    panelMappingsActions.add(buttonActionRemove);
                }
                panelMappings.add(panelMappingsActions, BorderLayout.NORTH);
            }
            tabbedPane.addTab("Port Mappings", panelMappings);

            {
                panelLog.setLayout(new BorderLayout());

                {
                    scrollPaneLog.setViewportView(textPaneLog);
                }
                panelLog.add(scrollPaneLog, BorderLayout.CENTER);
            }
            tabbedPane.addTab("Log", panelLog);

            {
                panelAbout.setLayout(new GridBagLayout());
                ((GridBagLayout)panelAbout.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)panelAbout.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)panelAbout.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)panelAbout.getLayout()).rowWeights = new double[] {1.0, 1.0, 1.0E-4};

                labelAbout.setText("one.papachi.nat4j:1.0.0");
                labelAbout.setHorizontalAlignment(SwingConstants.CENTER);
                panelAbout.add(labelAbout, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.SOUTH, GridBagConstraints.NONE,
                    new Insets(0, 0, 5, 0), 0, 0));

                labelLink.setText("<html><a href=\"https://github.com/papachi-one/nat4j\">https://github.com/papachi-one/nat4j</a></html>");
                labelLink.setHorizontalAlignment(SwingConstants.CENTER);
                labelLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                labelLink.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        labelLinkMouseClicked(e);
                    }
                });
                panelAbout.add(labelLink, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane.addTab("About", panelAbout);
        }
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        setSize(400, 400);
        setLocationRelativeTo(null);

        {
            dialogAddPortMapping.setTitle("Port Mapping");
            var dialogAddPortMappingContentPane = dialogAddPortMapping.getContentPane();
            dialogAddPortMappingContentPane.setLayout(new BorderLayout(5, 5));

            {
                panelAddPortMapping.setLayout(new FlowLayout(FlowLayout.RIGHT));

                buttonSave.setText("Save");
                buttonSave.addActionListener(e -> buttonSaveActionPerformed(e));
                panelAddPortMapping.add(buttonSave);

                buttonCancel.setText("Cancel");
                buttonCancel.addActionListener(e -> buttonCancelActionPerformed(e));
                panelAddPortMapping.add(buttonCancel);
            }
            dialogAddPortMappingContentPane.add(panelAddPortMapping, BorderLayout.SOUTH);

            {
                panelAddPortMappingForm.setBorder(new EmptyBorder(5, 5, 5, 5));
                panelAddPortMappingForm.setLayout(new GridBagLayout());
                ((GridBagLayout)panelAddPortMappingForm.getLayout()).columnWidths = new int[] {0, 100, 0};
                ((GridBagLayout)panelAddPortMappingForm.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panelAddPortMappingForm.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panelAddPortMappingForm.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                labelProtocol.setText("Protocol");
                labelProtocol.setHorizontalAlignment(SwingConstants.TRAILING);
                panelAddPortMappingForm.add(labelProtocol, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                comboBoxProtocol.setModel(new DefaultComboBoxModel<>(new String[] {
                    "TCP",
                    "UDP"
                }));
                panelAddPortMappingForm.add(comboBoxProtocol, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                labelInternalPort.setText("Internal Port");
                labelInternalPort.setHorizontalAlignment(SwingConstants.TRAILING);
                panelAddPortMappingForm.add(labelInternalPort, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

                spinnerInternalPort.setModel(new SpinnerNumberModel(8000, 1, 65535, 1));
                panelAddPortMappingForm.add(spinnerInternalPort, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                labelExternalPort.setText("External Port");
                labelExternalPort.setHorizontalAlignment(SwingConstants.TRAILING);
                panelAddPortMappingForm.add(labelExternalPort, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                spinnerExternalPort.setModel(new SpinnerNumberModel(8000, 1, 65535, 1));
                panelAddPortMappingForm.add(spinnerExternalPort, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogAddPortMappingContentPane.add(panelAddPortMappingForm, BorderLayout.CENTER);
            dialogAddPortMapping.pack();
            dialogAddPortMapping.setLocationRelativeTo(dialogAddPortMapping.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel panelStatus;
    private JLabel labelStatus;
    private JTabbedPane tabbedPane;
    private JPanel panelHome;
    private JPanel panelHomeForm;
    private JLabel labelIPv4;
    private JLabel labelIPv6;
    private JLabel labelLocalIP;
    private JTextField textFieldLocalIPv4;
    private JTextField textFieldLocalIPv6;
    private JLabel labelGatewayIP;
    private JTextField textFieldGatewayIPv4;
    private JTextField textFieldGatewayIPv6;
    private JLabel labelPublicIP;
    private JTextField textFieldPublicIPv4;
    private JTextField textFieldPublicIPv6;
    private JPanel panelMappings;
    private JScrollPane scrollPaneMappings;
    private JTable tableMappings;
    private JPanel panelMappingsActions;
    private JButton buttonActionAdd;
    private JButton buttonActionRemove;
    private JPanel panelLog;
    private JScrollPane scrollPaneLog;
    private JTextPane textPaneLog;
    private JPanel panelAbout;
    private JLabel labelAbout;
    private JLabel labelLink;
    private JDialog dialogAddPortMapping;
    private JPanel panelAddPortMapping;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JPanel panelAddPortMappingForm;
    private JLabel labelProtocol;
    private JComboBox<String> comboBoxProtocol;
    private JLabel labelInternalPort;
    private JSpinner spinnerInternalPort;
    private JLabel labelExternalPort;
    private JSpinner spinnerExternalPort;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
