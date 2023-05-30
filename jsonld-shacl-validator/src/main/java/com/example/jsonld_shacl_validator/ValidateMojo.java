package com.example.jsonld_shacl_validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.rdf.model.Model;  // Correct import
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.StringWriter;
import org.topbraid.shacl.vocabulary.SH;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.topbraid.shacl.validation.ValidationUtil;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.VALIDATE)

public class ValidateMojo extends AbstractMojo{
	@Parameter(property = "jsonldFile", defaultValue = "${basedir}/src/main/resources/data.jsonld")
	private File jsonldFile;
	
	 @Parameter(property = "shaclFile", defaultValue = "${basedir}/src/main/resources/Shapes.ttl")
	 private File shaclFile;
	 
	 public void execute() throws MojoExecutionException {
		    getLog().info("Validating JSON-LD data against SHACL shapes...");

		    if (!jsonldFile.exists() || !shaclFile.exists()) {
		        throw new MojoExecutionException("JSON-LD or SHACL files not found!");
		    }

		    try {
		        Model shapesModel = ModelFactory.createDefaultModel();
		        RDFDataMgr.read(shapesModel, new FileInputStream(shaclFile), null, Lang.TTL);

		        Model dataModel = ModelFactory.createDefaultModel();
		        RDFDataMgr.read(dataModel, new FileInputStream(jsonldFile), null, Lang.JSONLD);

		        Resource reportResource = ValidationUtil.validateModel(dataModel, shapesModel, false);
		        boolean conforms = reportResource.getProperty(SH.conforms).getBoolean();

		        if (!conforms) {
		            StringWriter writer = new StringWriter();
		            reportResource.getModel().write(writer, "TURTLE");
		            String report = writer.toString();
		            throw new MojoExecutionException("Validation failed: \n" + report);
		        }

		        getLog().info("Validation passed: No SHACL violations found.");
		    } catch (FileNotFoundException e) {
		        throw new MojoExecutionException("File not found!", e);
		    } catch (RiotException e) {
		        throw new MojoExecutionException("Error parsing JSON-LD or TTL!", e);
		    }
		}
}
