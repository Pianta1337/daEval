package fr.dawan.evalnico.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import fr.dawan.evalnico.dto.CountDto;
import fr.dawan.evalnico.dto.EtudiantDto;
import fr.dawan.evalnico.dto.LoginDto;
import fr.dawan.evalnico.dto.LoginResponseDto;
import fr.dawan.evalnico.dto.UtilisateurDto;
import fr.dawan.evalnico.entities.Etudiant;
import fr.dawan.evalnico.entities.Promotion;
import fr.dawan.evalnico.exceptions.InvalidDataException;
import fr.dawan.evalnico.exceptions.NoDataException;
import fr.dawan.evalnico.repositories.EtudiantRepository;
import fr.dawan.evalnico.repositories.EvaluationRepository;
import fr.dawan.evalnico.repositories.PromotionRepository;
import fr.dawan.evalnico.tools.DtoTools;
import fr.dawan.evalnico.tools.HashTools;

@Service
@Transactional
public class EtudiantServiceImpl implements EtudiantService {
	@Autowired
	private EtudiantRepository etudiantRepository;
	@Autowired
	private PromotionService promotionService;
	@Autowired
	private PromotionRepository promotionRepository;
	@Autowired
	private EvaluationRepository evaluationRepository;
	@Autowired
	private EvaluationService evaluationService;
	@Autowired
	private UtilisateurService utilisateurService;
	@Autowired
	private PositionnementService positionnementService;

	@Override
	public EtudiantDto saveOrUpdate(EtudiantDto uDto) throws Exception {
		Etudiant u = DtoTools.convert(uDto, Etudiant.class);

		if (uDto.getId() == 0) { // insertion
			try {
				UtilisateurDto uInDb = this.findByEmail(uDto.getEmail());
				if (uInDb != null) {
					throw new InvalidDataException("Email déjà pris");
				}
			} catch (NoDataException e) {

			}
			uDto.setMotDePasse(HashTools.hashSHA512(uDto.getMotDePasse()));
		} else {
			Optional<Etudiant> etudiantInDb = etudiantRepository.findById(uDto.getId());
			if (etudiantInDb.isPresent()) {
				// vérif que les mots de passe sont les meme sinon on le modifie
				if (u.getMotDePasse().length() > 0 && !etudiantInDb.get().getMotDePasse()
						.contentEquals(HashTools.hashSHA512(uDto.getMotDePasse()))) {

					u.setMotDePasse(HashTools.hashSHA512(uDto.getMotDePasse()));
				}
				if (u.getMotDePasse().length() == 0) {
					u.setMotDePasse(etudiantInDb.get().getMotDePasse());
				}
			}
		}

//		Etudiant u = DtoTools.convert(uDto, Etudiant.class);
		if (uDto.getPromotionsId() != null) {
			for (long id : uDto.getPromotionsId()) {
				Promotion promo = promotionRepository.findById(id).get();
				u.getPromotions().add(promo);
				promo.getEtudiants().add(u);
				u.getPromotions().remove(null);
			}

		}
		u = etudiantRepository.saveAndFlush(u);
		return DtoTools.convert(u, EtudiantDto.class);
	}

	@Override
	public List<EtudiantDto> getAll() {

		List<Etudiant> ListU = etudiantRepository.findAll();
		List<EtudiantDto> result = new ArrayList<EtudiantDto>();

		for (Etudiant utilisateur : ListU) {
			result.add(DtoTools.convert(utilisateur, EtudiantDto.class));
		}
		return result;
	}

	@Override
	public void delete(long id) throws NoDataException {
		// suppression des eval et des positionnements de l'etudiant
		Optional<Etudiant> e = etudiantRepository.findById(id);
		if (e.isPresent()) {
//			evaluationRepository.deleteByUserId(e.get().getId());
			evaluationService.deleteByUserId(e.get().getId());
			positionnementService.deleteByEtudiantId(id);
			etudiantRepository.deleteById(id);
		}else {
			throw new NoDataException("Student not found");
		}
	}

	@Override
	public EtudiantDto findByEmail(String email) throws Exception {
		Etudiant u = etudiantRepository.findByEmail(email);
		if (u != null)
			return DtoTools.convert(u, EtudiantDto.class);
		else
			throw new Exception("User not found !");
	}

	@Override
	public EtudiantDto getById(long id) throws NoDataException {
		if (id <= 0) {
			throw new IllegalArgumentException("Id must be greater than 0");
		}
		Optional<Etudiant> u = etudiantRepository.findById(id);
		if (u.isPresent())
			return DtoTools.convert(u.get(), EtudiantDto.class);

		throw new NoDataException("No student found");
	}

	@Override
	public List<EtudiantDto> getAll(int page, int max, String search) {
		// on requête la bdd
		List<Etudiant> users = etudiantRepository.findAllByNomContainingOrPrenomContainingOrEmailContaining(search,
				search, search, PageRequest.of(page, max)).get().collect(Collectors.toList());

		// on transforme le résultat en liste de DTO
		List<EtudiantDto> result = new ArrayList<EtudiantDto>();
		for (Etudiant u : users) {
			result.add(DtoTools.convert(u, EtudiantDto.class));
		}
		return result;
	}

	@Override
	public CountDto count(String search) {
		long result = etudiantRepository.countByNomContainingOrPrenomContainingOrEmailContaining(search, search,
				search);
		CountDto d = new CountDto();
		d.setNb(result);
		return d;
	}

	@Override
	public List<EtudiantDto> getEtudiantAyantPasseEpreuve(long epreuveId) {
		List<Etudiant> users = etudiantRepository.getEtudiantAyantPasseEpreuve(epreuveId);

		List<EtudiantDto> result = new ArrayList<EtudiantDto>();
		for (Etudiant u : users) {
			result.add(DtoTools.convert(u, EtudiantDto.class));
		}
		return result;
	}

	@Override
	public List<EtudiantDto> getEtudiantByInterventionId(long intervId) {
		List<Etudiant> users = etudiantRepository.getEtudiantsByInterventionId(intervId);

	
		List<EtudiantDto> result = new ArrayList<EtudiantDto>();
		for (Etudiant u : users) {
			result.add(DtoTools.convert(u, EtudiantDto.class));
		}
		return result;
	}

}
