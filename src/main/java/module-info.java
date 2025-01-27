// module com.mycompany.distributed_assessment1 {
//     requires javafx.controls;
//     requires javafx.fxml;
//     requires java.base;

//     opens com.mycompany.distributed_assessment1 to javafx.fxml;
//     exports com.mycompany.distributed_assessment1;
// }

module com.mycompany.distributed_assessment1 {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires mysql.connector.j;
    
    opens com.mycompany.distributed_assessment1 to javafx.fxml;
    exports com.mycompany.distributed_assessment1;
}
