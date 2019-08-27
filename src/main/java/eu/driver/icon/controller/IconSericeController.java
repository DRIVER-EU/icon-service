package eu.driver.icon.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.imageio.ImageIO;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.driver.icon.controller.util.IconMap;

@RestController
public class IconSericeController implements ResourceProcessor<RepositoryLinksResource>  {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
    private FileStorageService fileStorageService;
	
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		return resource;
	}

	public IconSericeController() {
		
	}
	
	@ApiOperation(value = "getIcon", nickname = "getIcon")
	@RequestMapping(value = "/TBIconService/getIcon", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "path", value = "the icon path that point to the icon", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "size", value = "the size of the icon, if not defined it is 32x32 pixel", required = false, dataType = "string", paramType = "query")})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<byte[]> getIcon(@QueryParam("path") String path, @QueryParam("size") String size) {
		log.info("--> getIcon: " + path + ", size: " + size);
		path = path.toUpperCase();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String key = path;
		if (size != null) {
			size = size.toLowerCase();
			key += "/" + size;
		}
		
		byte[] iconResult = null;
		
		BufferedImage mapIcon = IconMap.getInstance().getIcon(key);
		
		if (mapIcon != null) {
			try {
				log.info("Found icon in Map");
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				ImageIO.write(mapIcon, "png", outputStream);
				outputStream.flush();
				iconResult = outputStream.toByteArray();
				try {
					outputStream.close();
				} catch (Exception ce) { log.warn("stream not closed"); }
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.valueOf("image/png"));
			    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
			    log.info("getIcon-->");
			    return new ResponseEntity<byte[]>(iconResult, headers, HttpStatus.OK);
			} catch (Exception e) {
				log.error("Error generating the icon response from the map!", e);
			}
		} else {
			log.info("Requesting icon from filesystem.");
			
			String iconPath = "./icons/" + path + ".png";
			if (Files.notExists(Paths.get(iconPath))) {
				iconPath = "./icons/DFLT.png";
			}
			try {
				File file = new File(iconPath);
    			mapIcon = ImageIO.read(file);
    			iconResult = Files.readAllBytes(file.toPath());
    			
				if (size != null) {
		    		// resize the image
		    		StringTokenizer tokens = new StringTokenizer(size, "x");
		    		if (tokens.countTokens() == 2) {
		    			BufferedImage resized = resizeImage(mapIcon, Integer.parseInt(tokens.nextToken()), Integer.parseInt(tokens.nextToken()));
		    			IconMap.getInstance().addIcon(key, resized);
		    			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						ImageIO.write(resized, "png", outputStream);
						outputStream.flush();
						iconResult = outputStream.toByteArray();
						try {
							outputStream.close();
						} catch (Exception ce) { log.warn("stream not closed"); }
		    		} else {
		    			IconMap.getInstance().addIcon(key, mapIcon);
		    		}
		    	} else {
		    		iconResult = Files.readAllBytes(file.toPath());
	    			mapIcon = ImageIO.read(file);
	    			IconMap.getInstance().addIcon(key, mapIcon);
		    	}
			} catch (Exception e) {
				log.error("Error loading file and storing to icon map!", e);
			}
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.valueOf("image/png"));
		    headers.setCacheControl(CacheControl.noCache().getHeaderValue());
		    log.info("getIcon-->");
		    return new ResponseEntity<byte[]>(iconResult, headers, HttpStatus.OK);
		}
		
		log.info("getIcon -->");
		return new ResponseEntity<byte[]>("".getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ApiOperation(value = "uploadFile", nickname = "uploadFile")
	@RequestMapping(value = "/TBIconService/uploadFile", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "path", value = "the icon path that point to the icon", required = true, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "file", value = "the file to be uploaded", required = true, dataType = "__file")})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<String> uploadFile(@QueryParam("path") String path, @RequestPart("file") MultipartFile file) {
		
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String fileName = fileStorageService.storeFile(path, file);
		
		return new ResponseEntity<String>(fileName, HttpStatus.OK);
		
	}
	//public  uploadIcon(@QueryParam("path") String path, @QueryParam("size") String size) {
	
	private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
	 
		return resizedImage;
	}

}
