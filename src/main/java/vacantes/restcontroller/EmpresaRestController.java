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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import vacantes.jwt.JwtTokenUtil;
import vacantes.modelo.dto.EmpresaModificarDto;
import vacantes.modelo.dto.SolicitudListasDto;
import vacantes.modelo.dto.VacanteAltaDto;
import vacantes.modelo.dto.VacanteDetalleDto;
import vacantes.modelo.dto.VacanteListasDto;
import vacantes.modelo.dto.VacanteModificarDto;
import vacantes.modelo.entities.Categoria;
import vacantes.modelo.entities.Empresa;
import vacantes.modelo.entities.Estatus;
import vacantes.modelo.entities.Solicitud;
import vacantes.modelo.entities.Vacante;
import vacantes.modelo.service.CategoriaService;
import vacantes.modelo.service.EmpresaService;
import vacantes.modelo.service.SolicitudService;
import vacantes.modelo.service.VacanteService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/empresa")
public class EmpresaRestController {

	@Autowired
	private EmpresaService empresaService;

	@Autowired
	private VacanteService vacanteService;

	@Autowired
	private SolicitudService solicitudService;
	
	@Autowired
	private CategoriaService categoriaService;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@GetMapping("/verTodasVacantes")
	public ResponseEntity<?> verTodasVacantes(HttpServletRequest request) {
	    String authHeader = request.getHeader("Authorization");

	    if (authHeader != null && authHeader.startsWith("Bearer ")) {
	        String token = authHeader.substring(7);
	        String email = jwtTokenUtil.getUsernameFromToken(token);

	        Empresa empresa = empresaService.buscarEmpresaPorEmail(email);

            List<Vacante> vacantes = vacanteService.buscarVacantePorEmpresa(empresa.getIdEmpresa());
            
            List<VacanteListasDto> vacantesDto = vacantes.stream()
            		.map(vacante ->{
            			VacanteListasDto dto = new VacanteListasDto();
            			return dto.convertToVacanteListasDto(vacante);
            		})
            			.collect(Collectors.toList());
                    
            return new ResponseEntity<>(vacantesDto, HttpStatus.OK);
	        }
	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
	}
	
	@PutMapping("/modificarEmpresa")
	public ResponseEntity<?> modificarEmpresa(@RequestBody EmpresaModificarDto empresaDto, HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			String email = jwtTokenUtil.getUsernameFromToken(token);
			
			Empresa empresaActual = empresaService.buscarEmpresaPorEmail(email);
				
			empresaActual.setNombreEmpresa(empresaDto.getNombre());
			empresaActual.setDireccionFiscal(empresaDto.getDireccionFiscal());
			empresaActual.setPais(empresaDto.getPais());
			
			if (empresaService.modificarUno(empresaActual) == 1) {
				return new ResponseEntity<>("La empresa se modificó correctamente", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("La empresa no se ha podido modificar", HttpStatus.NOT_MODIFIED);
			}
			
		} else 
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
	}

	@PostMapping("/altaVacante")
	public ResponseEntity<?> altaVacante(@RequestBody VacanteAltaDto altaDto, HttpServletRequest request) {
	    String authHeader = request.getHeader("Authorization");
	    if (authHeader != null && authHeader.startsWith("Bearer ")) {
	        String token = authHeader.substring(7);
	        String email = jwtTokenUtil.getUsernameFromToken(token);

	        Empresa empresaB = empresaService.buscarEmpresaPorEmail(email);
	        Categoria categoriaBuscada = categoriaService.buscarUno(altaDto.getIdCategoria());

	        if (categoriaBuscada == null) {
	            return new ResponseEntity<>("La categoría no existe", HttpStatus.INTERNAL_SERVER_ERROR);
	        }else {
	        	Vacante nuevaVacante = new Vacante();
		        nuevaVacante.setEmpresa(empresaB);
		        nuevaVacante.setCategoria(categoriaBuscada);
		        nuevaVacante.setNombre(altaDto.getNombre());
		        nuevaVacante.setDescripcion(altaDto.getDescripcion());
		        nuevaVacante.setFecha(new Date());
		        nuevaVacante.setEstatus(Estatus.CREADA);
		        nuevaVacante.setSalario(altaDto.getSalario());
		        nuevaVacante.setDetalles(altaDto.getDetalles());
		        nuevaVacante.setImagen(altaDto.getImagen());

		        vacanteService.insertUno(nuevaVacante);
		        return new ResponseEntity<>("Vacante creada con éxito", HttpStatus.CREATED);
	        } 
	    } else {
	        return new ResponseEntity<>("No autorizado", HttpStatus.UNAUTHORIZED);
	    }
	}

	@PutMapping("/eliminarVacante/{idVacante}")
	public ResponseEntity<?> eliminar(@PathVariable int idVacante, HttpServletRequest request) {
		try {
			boolean token = isValidToken(request);

			if (token == false) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
			}
			vacanteService.cancelVacante(idVacante);
			return new ResponseEntity<>("La vacante se ha cancelado con éxito", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Vacante no eliminada" + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/modificarVacante/{idVacante}")
	public ResponseEntity<?> modificarVacante(@PathVariable int idVacante, @RequestBody VacanteModificarDto modificarDto,
			HttpServletRequest request) {
		try {
			boolean token = isValidToken(request);

			if (token == false) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
			}
			Vacante vacanteMod = vacanteService.buscarUno(idVacante);
			
			vacanteMod.setIdVacante(idVacante);
			vacanteMod.setNombre(modificarDto.getNombre());
			vacanteMod.setDescripcion(modificarDto.getDescripcion());
			vacanteMod.setSalario(modificarDto.getSalario());
			vacanteMod.setEstatus(modificarDto.getEstatus());
			vacanteMod.setImagen(modificarDto.getImagen());
			vacanteMod.setDetalles(modificarDto.getDetalles());
			
			if (vacanteService.modificarUno(vacanteMod) == 1) {
				return new ResponseEntity<>("La vacante se modificó correctamente", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("La vacante no se ha podido modificar", HttpStatus.NOT_MODIFIED);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Vacante no modificada" + e.getMessage(), HttpStatus.NOT_MODIFIED);
		}
	}

	@GetMapping("/detalleVacante/{idVacante}")
	public ResponseEntity<?> verDetalle(@PathVariable int idVacante, HttpServletRequest request) {
		try {
			boolean token = isValidToken(request);

			if (token == false) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
			}
			
			Vacante vacanteBuscada = vacanteService.buscarUno(idVacante);
			
			if(vacanteBuscada != null) {
				VacanteDetalleDto vacanteDetalle = new VacanteDetalleDto();
				
				return new ResponseEntity<>(vacanteDetalle.convertToVacanteDetalleDto(vacanteBuscada), HttpStatus.FOUND);
			}
			
			return new ResponseEntity<>("La vacante no se ha podido encontrar", HttpStatus.FOUND);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Token inválido" + e.getMessage(), HttpStatus.NOT_FOUND);
		}
	}
	
	@PutMapping("/asignarVacante/{idVacante}/{email}")
	public ResponseEntity<?> asignarCandidato(@PathVariable int idVacante, @PathVariable String email,
			HttpServletRequest request) {

		try {

			boolean token = isValidToken(request);

			if (token == false) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
			}

			Vacante vacanteBuscada = vacanteService.buscarUno(idVacante);
			vacanteBuscada.setEstatus(Estatus.CUBIERTA);
			vacanteService.modificarUno(vacanteBuscada);

			Solicitud solicitudBuscada = solicitudService.buscarSolicitudPorIdVacanteYEmail(idVacante, email);
			solicitudBuscada.setEstado(true);
			solicitudService.modificarUno(solicitudBuscada);
			return new ResponseEntity<>("Solicitud asignada", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("La solicitud no se ha podido asignar", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/verTodasSolicitudes")
	public ResponseEntity<?> verTodasSolicitudes(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			String email = jwtTokenUtil.getUsernameFromToken(token);

			Empresa empresa = empresaService.buscarEmpresaPorEmail(email);

				if (empresa != null) {

					List<Solicitud> solicitudes = solicitudService.buscarSolicitudPorEmpresa(empresa.getIdEmpresa());
					
					List<SolicitudListasDto> solicitudDto = solicitudes.stream()
		            		.map(solicitud ->{
		            			SolicitudListasDto dto = new SolicitudListasDto();
		            			return dto.convertToSolicitudDto(solicitud);
		            		})
		            			.collect(Collectors.toList());
		                    
		            return new ResponseEntity<>(solicitudDto, HttpStatus.OK);
			}
	}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
	}		

	@GetMapping("/verCV/{idSolicitud}")
	public ResponseEntity<?> verCV(@PathVariable int idSolicitud, HttpServletRequest request) {
		try {
			boolean token = isValidToken(request);

			if (token == false) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
			}
			Solicitud solicitudBuscada = solicitudService.buscarUno(idSolicitud);

			return new ResponseEntity<>(solicitudBuscada.getCurriculum(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("No se encontro la solicitud" + e.getMessage(), HttpStatus.NOT_FOUND);
		}
	}

	@PutMapping("/aceptarSolicitud/{idSolicitud}")
	public ResponseEntity<?> aceptarSolicitud(@PathVariable int idSolicitud, HttpServletRequest request) {
		try {
			boolean token = isValidToken(request);

			if (token == false) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
			}

			Solicitud solicitudBuscada = solicitudService.buscarUno(idSolicitud);
			if (solicitudBuscada != null) {
				solicitudBuscada.setEstado(true);
				solicitudService.modificarUno(solicitudBuscada);
				return new ResponseEntity<>("La solicitud se aceptó", HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<>("La solicitud no pudo acptarse", HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("No se ha podido aceptar la solicitud", HttpStatus.NOT_MODIFIED);
		}
	}

	@PutMapping("/denegarSolicitud/{idSolicitud}")
	public ResponseEntity<?> denegarSolicitud(@PathVariable int idSolicitud, HttpServletRequest request) {
		try {
			boolean token = isValidToken(request);

			if (token == false) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
			}

			Solicitud solicitudBuscada = solicitudService.buscarUno(idSolicitud);
			if (solicitudBuscada != null) {
				solicitudBuscada.setEstado(false);
				solicitudService.modificarUno(solicitudBuscada);
				return new ResponseEntity<>("La solicitud se denegó", HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<>("La solicitud no pudo ser denegada", HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("No se ha podido denegar la solicitud", HttpStatus.NOT_MODIFIED);
		}
	}

	@PutMapping("/escribirComentario/{idSolicitud}")
	public ResponseEntity<?> escribirComentario(@RequestBody String comentarios, @PathVariable int idSolicitud,
			HttpServletRequest request) {
		try {
			boolean token = isValidToken(request);

			if (token == false) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no válido o no proporcionado");
			}
			Solicitud solicitudBuscada = solicitudService.buscarUno(idSolicitud);
			if (solicitudBuscada != null) {
				solicitudBuscada.setComentarios(comentarios);
				solicitudService.modificarUno(solicitudBuscada);
				return new ResponseEntity<>("Los comentarios se publicaron con éxito", HttpStatus.OK);
			}
			return new ResponseEntity<>("Los comentarios no se publicaron", HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("No se han introducido comentarios" + e.getMessage(), HttpStatus.NOT_MODIFIED);
		}
	}

	private boolean isValidToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			return jwtTokenUtil.validateToken(token);
		}

		return false;
	}

}
