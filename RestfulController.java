import java.util.ArrayList;
import java.util.List;

import com.hetos.media.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.rail.eseva.entity.Customer;
import com.rail.eseva.exception.ApplicationException;
import com.rail.eseva.exception.DataNotFoundException;
import com.rail.eseva.facade.ClientFacade;
import com.rail.eseva.utiliyi.URIConstants;

@RestController(value="restfulController")
public class RestfulController {

	@Autowired
	private ClientFacade clientFacade;
	
	 @RequestMapping(value=URIConstants.FETCH_USER_BY_ID, method=RequestMethod.GET )
	public @ResponseBody List<Client>fetchClientById(@PathVariable("id") Long entityId) {
		List<Customer> result = clientFacade.fetchCustomerById(entityId);
		List<Client> clients = mapToClient(result);
		
		return clients;
	} 
	
	 /**
	  * GET: fetch all customer data using webservice
	  * @return
	  */
	@RequestMapping(value=URIConstants.FETCH_ALL, method=RequestMethod.GET)
	public ResponseEntity<List<Client>> getAllCustomer() {
		List<Customer> result = clientFacade.getAllCustomer();
		List<Client> clients = mapToClient(result);
		
		if(result.size() > 0) {
			//return ResponseEntity.ok().header("userid", "password").body(clients);
			return new ResponseEntity<List<Client>>(clients, HttpStatus.OK);
		}else{
			return new ResponseEntity<List<Client>>(HttpStatus.NOT_FOUND);
		}
		 
	}
	
	
	/**
	 * Post:Request to create new client using webservice 
	 * @param customer
	 * @param componentBuilder
	 * @return
	 */
	@RequestMapping(value=URIConstants.CRAETE_NEW_CLIENT, method=RequestMethod.POST)
	public ResponseEntity<Void> addClient(@RequestBody Customer customer, UriComponentsBuilder componentBuilder) {
		if(isUserExits(customer)) {
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		}
		
		try {
			clientFacade.addCustomer(customer);
		} catch (ApplicationException e) {
			e.printStackTrace();
		}
		
		HttpHeaders header = new HttpHeaders();
		header.setLocation(componentBuilder.path("/addCustomer/{id}").buildAndExpand(customer.getId()).toUri());
		return new ResponseEntity<Void>(header, HttpStatus.CREATED);
	}
	
	/**
	 * PUT: request to update user data
	 * @param customer
	 * @return
	 */
	@RequestMapping(value=URIConstants.UPDATE_USER, method=RequestMethod.PUT)
	public ResponseEntity<Client> updateClient(@RequestBody Customer customer) {
		if(isUserExits(customer)) {
			clientFacade.updateCustomer(mapCustomerRecord(customer));
			return new ResponseEntity<Client>(getClientFromCustomer(customer), HttpStatus.OK);
		}else{
			return new ResponseEntity<Client>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * DELETE: delete customer from DB using webserviec
	 * @param userId
	 * @return
	 * @throws DataNotFoundException 
	 */
	@RequestMapping(value=URIConstants.REMOVE_USER, method=RequestMethod.DELETE)
	public ResponseEntity<Client> deleteUser(@PathVariable("id") Long userId) {
		List<Customer> customer = clientFacade.fetchCustomerById(userId);
		if(customer.size() < 1) {
			  
			return new ResponseEntity<Client>(HttpStatus.NO_CONTENT);
		} else{ 
			clientFacade.remove(userId);
			return new ResponseEntity<Client>(HttpStatus.OK);
		}
		 
	}