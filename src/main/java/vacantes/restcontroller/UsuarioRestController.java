package vacantes.restcontroller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import vacantes.jwt.JwtTokenUtil;
import vacantes.modelo.dto.SolicitudAltaDto;
import vacantes.modelo.dto.SolicitudListasDto;
import vacantes.modelo.dto.UsuarioAltaDto;
import vacantes.modelo.dto.VacanteListasDto;
import vacantes.modelo.entities.Rol;
import vacantes.modelo.entities.Solicitud;
import vacantes.modelo.entities.Usuario;
import vacantes.modelo.entities.Vacante;
import vacantes.modelo.service.SolicitudService;
import vacantes.modelo.service.UsuarioService;
import vacantes.modelo.service.VacanteService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/usuario")
public class UsuarioRestController {

	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private VacanteService vacanteService;
	
	@Autowired
	private SolicitudService solicitudService;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
//	@Autowired
//	private ModelMapper modelMapper;
	
	@PostMapping("/altaUsuario")
	public ResponseEntity<?> altaUsuario(@RequestBody UsuarioAltaDto altaDto, HttpServletRequest request) {
	    try {
	        boolean token = isValidToken(request);
	        if (!token) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
	        }
	        
	        Usuario usuarioNuevo = new Usuario();
	        
	        usuarioNuevo.setEmail(altaDto.getEmail());
	        usuarioNuevo.setNombre(altaDto.getNombre());
	        usuarioNuevo.setApellidos(altaDto.getApellidos());
	        usuarioNuevo.setPassword(altaDto.getPassword());
	        usuarioNuevo.setEnabled(1);
	        usuarioNuevo.setFechaRegistro(new Date());
	        usuarioNuevo.setRol(Rol.CLIENTE);
	        
	        if(usuarioService.insertUno(usuarioNuevo) == true);
	        	return new ResponseEntity<>("El usuario se creó correctamente", HttpStatus.CREATED);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new ResponseEntity<>("Error al dar de alta el usuario: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@GetMapping("/verVacanteCreada")
	public ResponseEntity<?> verCreadas(HttpServletRequest request){
		 try {
		        boolean token = isValidToken(request);
		        if (!token) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
		        }
		       
		       List<Vacante> listaux =  vacanteService.buscarPorCreada();
		       
		       if(!listaux.isEmpty()) {
		    	   List<VacanteListasDto> vacanteDto = listaux.stream()
		            		.map(vacante ->{
		            			VacanteListasDto dto = new VacanteListasDto();
		            			return dto.convertToVacanteListasDto(vacante);
		            		})
		            			.collect(Collectors.toList());
		                    
		            return new ResponseEntity<>(vacanteDto, HttpStatus.OK);
		       }
		       return new ResponseEntity<>("No se han encontrado vacantes asociadas", HttpStatus.OK);
		 } catch (Exception e) {
		        e.printStackTrace();
		        return new ResponseEntity<>("Token inválido" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		    }
	}
	
	@GetMapping("/verCreadaEmpresa/{idEmpresa}")
	public ResponseEntity<?> verCreadasEmpresa(@PathVariable int idEmpresa,HttpServletRequest request){
		 try {
		        boolean token = isValidToken(request);
		        if (!token) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
		        }
		        List<Vacante> listaux = vacanteService.buscarPorCreadaYEmpresa(idEmpresa);
		        
		        if(!listaux.isEmpty()) {
			    	   List<VacanteListasDto> vacanteDto = listaux.stream()
			            		.map(vacante ->{
			            			VacanteListasDto dto = new VacanteListasDto();
			            			return dto.convertToVacanteListasDto(vacante);
			            		})
			            			.collect(Collectors.toList());
			                    
			            return new ResponseEntity<>(vacanteDto, HttpStatus.OK);
			       }
		        return new ResponseEntity<>("No se encuentran vacantes disponibles", HttpStatus.BAD_REQUEST);

		 } catch (Exception e) {
		        e.printStackTrace();
		        return new ResponseEntity<>("Token inválido" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		    }
	}
	
	@GetMapping("/verCreadaCategoria/{idCategoria}")
	public ResponseEntity<?> verCreadasCategoria(@PathVariable int idCategoria, HttpServletRequest request){
		 try {
		        boolean token = isValidToken(request);
		        if (!token) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
		        }
		        
		        List<Vacante> listaux = vacanteService.buscarPorCreadaYCategoria(idCategoria);
		        
		        if(!listaux.isEmpty()) {
			    	   List<VacanteListasDto> vacanteDto = listaux.stream()
			            		.map(vacante ->{
			            			VacanteListasDto dto = new VacanteListasDto();
			            			return dto.convertToVacanteListasDto(vacante);
			            		})
			            			.collect(Collectors.toList());
			                    
			            return new ResponseEntity<>(vacanteDto, HttpStatus.OK);
			    }
		        return new ResponseEntity<>("No se encuentran vacantes disponibles", HttpStatus.BAD_REQUEST);

		 } catch (Exception e) {
		        e.printStackTrace();
		        return new ResponseEntity<>("Token inválido" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		    }
	}
	
	@GetMapping("/verSolicitudes")
	public ResponseEntity<?>verSolicitudes(HttpServletRequest request){
		String authHeader = request.getHeader("Authorization");
			
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				String email = jwtTokenUtil.getUsernameFromToken(token);
				
				List<Solicitud> listaux = solicitudService.buscarSolicitudPorIdUsuario(email);
				
				if(!listaux.isEmpty()) {
			    	   List<SolicitudListasDto> solicitudDto = listaux.stream()
			            		.map(solicitud ->{
			            			SolicitudListasDto dto = new SolicitudListasDto();
			            			return dto.convertToSolicitudDto(solicitud);
			            		})
			            			.collect(Collectors.toList());
			            return new ResponseEntity<>(solicitudDto, HttpStatus.OK);
			    }
		        return new ResponseEntity<>("No se encuentran vacantes disponibles", HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>("No se han encontrado solicitudes", HttpStatus.NOT_FOUND);
	}

	@PostMapping("/postularVacante/{idVacante}")
    public ResponseEntity<?> postularVacante(@PathVariable int idVacante ,@RequestBody SolicitudAltaDto altaDto,HttpServletRequest request){

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtTokenUtil.getUsernameFromToken(token);

            Usuario usuarioPostulante = usuarioService.buscarPorEmailEntidad(email);
            Vacante vacanteSolicitud = vacanteService.buscarUno(idVacante);

            if (vacanteSolicitud == null) {
                return new ResponseEntity<>("La vacante no existe", HttpStatus.INTERNAL_SERVER_ERROR);
            }else {
                Solicitud solicitudPostulante = new Solicitud();

                  solicitudPostulante.setUsuario(usuarioPostulante);
                  solicitudPostulante.setVacante(vacanteSolicitud);
                  solicitudPostulante.setFecha(new Date());
                  solicitudPostulante.setArchivo(altaDto.getArchivo());
                  solicitudPostulante.setComentarios(altaDto.getComentarios());
                  solicitudPostulante.setEstado(true);
                  solicitudPostulante.setCurriculum(altaDto.getCurriculum());

                  solicitudService.insertUno(solicitudPostulante);
                  return new ResponseEntity<>("Solicitud creada con éxito", HttpStatus.OK);

            }
        } else {
            return new ResponseEntity<>("No autorizado", HttpStatus.UNAUTHORIZED);
        }
    }

	private boolean isValidToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7); // Eliminar "Bearer "
			return jwtTokenUtil.validateToken(token);
		}

		return false; // No hay token o no tiene el formato esperado
	}

}
