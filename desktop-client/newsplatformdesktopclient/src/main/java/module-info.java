module com.newsplatformdesktopclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.net.http;   // Pour HttpClient SOAP
    requires java.xml;        // Pour DocumentBuilder XML

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.newsplatformdesktopclient to javafx.fxml;
    exports com.newsplatformdesktopclient;
}