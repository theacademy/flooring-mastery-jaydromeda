package flooringmastery;

import controller.FlooringController;
import dao.OrderDao;
import dao.OrderDaoImpl;
import dao.ProductDao;
import dao.ProductDaoImpl;
import dao.TaxesDao;
import dao.TaxesDaoImpl;
import service.FlooringService;
import service.FlooringServiceImpl;
import ui.FlooringView;
import ui.UserIO;
import ui.UserIOConsoleImpl;

public class App {

    public static void main(String[] args) {
        //DAOs
        OrderDao orderDao = new OrderDaoImpl();
        ProductDao productDao = new ProductDaoImpl();
        TaxesDao taxesDao = new TaxesDaoImpl();

        //Service
        //Service needs daos as parameters
        FlooringService service = new FlooringServiceImpl(orderDao, productDao, taxesDao);

        //UI
        //UI needs view as parameter
        UserIO io = new UserIOConsoleImpl();
        FlooringView view = new FlooringView(io);

        //Controller
        //Controller needs the view and the service as parameters
        FlooringController controller = new FlooringController(service, view);
        controller.run();
    }
}