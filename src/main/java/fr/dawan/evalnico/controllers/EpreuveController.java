package fr.dawan.evalnico.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.dawan.evalnico.dto.CountDto;
import fr.dawan.evalnico.dto.EpreuveDto;
import fr.dawan.evalnico.dto.EtudiantDto;
import fr.dawan.evalnico.services.EpreuveService;
import fr.dawan.evalnico.tools.DtoTools;

@RestController
@RequestMapping("/api/epreuve")
public class EpreuveController {

	@Autowired
	private EpreuveService epreuveService;
	

	@PostMapping(consumes="application/json", produces = "application/json")
	public ResponseEntity<EpreuveDto> save(@RequestBody EpreuveDto epreuveDto){
		
		EpreuveDto result = epreuveService.saveOrUpdate(epreuveDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}
	
	@PutMapping(consumes="application/json", produces = "application/json")
	public ResponseEntity<EpreuveDto> update(@RequestBody EpreuveDto epreuveDto ){
		EpreuveDto result = epreuveService.saveOrUpdate(epreuveDto);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@DeleteMapping(value= "/{id}") 
	public ResponseEntity<Long> Delete(@PathVariable(name="id",required=true) long id){
		epreuveService.delete(id);
		return ResponseEntity.status(HttpStatus.OK).body(id);
	}
	
	@GetMapping(produces="application/json")
	public ResponseEntity<List<EpreuveDto>> getAll(){
		List<EpreuveDto> result = epreuveService.findAll();
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping(value= "/blocCompId={id}",produces="application/json")
	public ResponseEntity<List<EpreuveDto>> getByBlocCompId(@PathVariable(name="id",required=true) long id){
		List<EpreuveDto> result = epreuveService.findByBlocCompId(id);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping(value="/{id}",produces="application/json")
	public ResponseEntity<EpreuveDto> getById(@PathVariable(name="id",required=true) long id){
		EpreuveDto epreuve = epreuveService.getById(id);
		if(epreuve!=null) {
			return ResponseEntity.status(HttpStatus.OK).body(DtoTools.convert(epreuve, EpreuveDto.class));
		}else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
	}
	
	//Solution 2 avec PathVariable et dupliquer les URI
	@GetMapping(value= {"/page/{page}/{size}", "/page/{page}/{size}/{search}"}, produces = "application/json")
	public  ResponseEntity<List<EpreuveDto>> getAllByPage(
			@PathVariable("page") int page, 
			@PathVariable("size") int max, 
			@PathVariable(value="search", required = false) Optional<String> search){
		if(search.isPresent()) {
			List<EpreuveDto> result =  epreuveService.findAllPaged(page-1, max, search.get());
			return ResponseEntity.status(HttpStatus.OK).body(result);
		}
			
		else {
			List<EpreuveDto> result =  epreuveService.findAllPaged(page-1, max, "");
			return ResponseEntity.status(HttpStatus.OK).body(result);
		}
			
	}

	// GET /count/{search}
	@GetMapping(value= {"/count","/count/{search}"}, produces = "application/json")
	public CountDto countBy(@PathVariable(value = "search",required = false) Optional<String> search) {
		CountDto result = null;
		if(search.isPresent())
			result = epreuveService.count(search.get());
		else
			result = epreuveService.count("");

		return result;
	}
	
	
}
