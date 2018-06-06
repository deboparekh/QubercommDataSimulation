package com.semaifour.facesix.web;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semaifour.facesix.data.entity.elasticsearch.Entity;
import com.semaifour.facesix.data.entity.elasticsearch.EntityService;
import com.semaifour.facesix.domain.Message;
import com.semaifour.facesix.fsql.FSField;
import com.semaifour.facesix.fsql.FSql;
import com.semaifour.facesix.rest.EntityRestController;
import com.semaifour.facesix.rest.FSqlRestController;

/**
 * 
 * Configuration Controller for the webapp
 * 
 * @author mjs
 *
 */
@Controller
@RequestMapping("/web/entity")
public class EntityWebController extends WebController {

	static Logger LOG = LoggerFactory.getLogger(EntityWebController.class.getName());

	@Autowired
	EntityService service;

	@Autowired
	FSqlRestController fsqlRestController;

	@Autowired
	EntityRestController entityRestController;

	/**
	 * 
	 * Lists all entitys
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/list")
	public String list(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
		super.pre(model, request, response);
		model.put("time", new Date());
		Page<Entity> entitys = service.findAll(new PageRequest(0, 100));
		model.put("fsobjects", entitys);
		model.put("entity", TAB_HIGHLIGHTER);

		return "entity-list";
	}

	/**
	 * 
	 * 
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping("/explore/{euid}")
	public String explore(Map<String, Object> model, @PathVariable("euid") String euid,
			@RequestParam(value = "qs", required = false) String querystring,
			@RequestParam(value = "head", required = false, defaultValue = "false") boolean head,
			HttpServletRequest request, HttpServletResponse response) {
		model.put("head", head);
		if (euid != null
				&& (euid.equalsIgnoreCase("_URL_") || euid.equalsIgnoreCase("URI") || euid.equalsIgnoreCase("PATH"))) {
			try {
				response.sendRedirect(querystring + "&head=" + head);
			} catch (IOException e) {
			}
		}
		Entity en = service.findOneByUid(euid);

		if (en != null) {
			model.put("entity", en);
			model.put("querystring", querystring);
			String fsql = en.getSettings().getProperty("fsql");
			List<Map<String, Object>> list = entityRestController.query(euid, querystring);
			FSql fsqlo = FSql.parse(fsql);
			if (fsqlo.output().get("sort") != null) {
				String[] sort = fsqlo.output().get("sort").split(":");
				FSField f = fsqlo.getFiled(sort[0].trim());
				if (f != null) {
					model.put("sortfield", f.getRindex());
					model.put("sortorder", sort.length > 1 ? sort[1].toLowerCase() : "asc");
				}
			}
			if (list.size() > 0) {
				model.put("fscols", fsqlo.getFieldNames());
			}
			model.put("fsobjects", list);

			try {
				if (en.getJedittings() != null) {
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> map = mapper.readValue(en.getJedittings(),
							new TypeReference<HashMap<String, Object>>() {
							});
					List<Map<Object, Object>> enlinks = (List<Map<Object, Object>>) map.get("enlinks");
					if (enlinks.size() > 0) {
						if (enlinks.get(0).size() > 0) {
							model.put("enlinks", (List<Map<Object, Object>>) map.get("enlinks"));
						}
					}
				}
			} catch (Exception e) {
				LOG.info("Exception loding & parsing jedittings", e);
			}

		}
		return "entity-explore";
	}

	/**
	 * 
	 * Copies given entity to another
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping("/copy")
	public String open(Map<String, Object> model, @RequestParam(value = "id") String id) {
		model.put("time", new Date());
		Entity entity = null;
		if (id != null) {
			entity = service.findById(id);
			if (entity == null) {
				model.put("message", Message.newError("Entity not found for copy, please enter new entity details"));
			} else {
				// entity = new Entity();
				entity.setId(null);
				entity.setUid("Copy of " + entity.getUid());
				entity.setName("Copy of " + entity.getName());
				model.put("message", Message.newInfo("Please correct copied details and save to persist."));
			}
		} else {
			model.put("message", Message.newError("No Entity to copy, please enter new Entity details"));
		}

		model.put("fsobject", entity);
		model.put("entity", TAB_HIGHLIGHTER);

		return "entity-edit";
	}

	/**
	 * 
	 * Open a given entity
	 * 
	 * @param model
	 * @param id
	 * @param uid
	 * @param name
	 * @return
	 */
	@RequestMapping("/open")
	public String open(Map<String, Object> model, @RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "name", required = false) String name) {
		model.put("time", new Date());
		Entity entity = null;
		if (id != null) {
			entity = service.findById(id);
		} else if (uid != null) {
			entity = service.findOneByUid(uid);
		} else if (name != null) {
			// fetch config by namne
			entity = service.findOneByName(name);
		} else {
			model.put("message", Message.newInfo("Please enter new Entity details correctly"));
		}

		if (entity != null) {
			model.put("disabled", "disabled");
			model.put("message", Message.newInfo("Please update existing Entity config correctly"));
		}

		model.put("fsobject", entity);
		model.put("entity", TAB_HIGHLIGHTER);

		return "entity-edit";
	}

	/**
	 * Deletes the Id
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping("/delete")
	public String delete(Map<String, Object> model, @RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "uid", required = false) String uid) {
		Entity entity = service.findById(id);
		if (entity == null && uid != null) {
			entity = service.findOneByUid(uid);
		} else {
			entity = new Entity();
			entity.setId(id);
			entity.setUid(uid);
		}
		service.delete(entity);
		entity.setId(null);
		model.put("fsobject", entity);
		model.put("message", Message.newError("Entity deleted successfully"));
		model.put("entity", TAB_HIGHLIGHTER);

		return "entity-edit";
	}

	private Map<String, Object> tomap(String addattrs) {
		Map<String, Object> params = new HashMap<String, Object>();
		if (!StringUtils.isEmpty(addattrs)) {
			for (String kv : addattrs.split("&")) {
				try {
					String[] v = kv.split(":");
					params.put(v[0], v[1]);
				} catch (Exception e) {
					LOG.warn("addattrs parse failed :" + kv);
				}
			}
		}
		return params;
	}

	/**
	 * Saves entitys
	 * 
	 * @param model
	 * @param newen
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(Map<String, Object> model, @ModelAttribute Entity newen) {
		model.put("time", new Date());
		boolean shouldSave = true;
		if (newen.getId() == null) {
			newen.setCreatedOn(new Date());
			newen.setModifiedOn(new Date());

			if (StringUtils.isEmpty(newen.getUid()) || StringUtils.isEmpty(newen.getName())) {
				model.put("message", Message.newError("UID or Name can not be blank."));
				shouldSave = false;
			} else if (service.exists(newen.getUid(), newen.getName())) {
				model.put("message", Message.newError("Entity with UID or Name already exists."));
				shouldSave = false;
			}
		} else {
			// it's existing
			Entity olden = service.findById(newen.getId());
			if (olden == null) {
				model.put("message", Message.newFailure("Entity not found with ID :" + newen.getId()));
				shouldSave = false;
			} else {
				olden.setJedittings(newen.getJedittings());
				olden.setSettings(newen.getSettings());
				olden.setModifiedOn(new Date());
				olden.setTypefs(newen.getTypefs());
				olden.setDescription(newen.getDescription());
				newen = olden;
			}
		}

		if (shouldSave) {
			newen = service.save(newen);

			model.put("disabled", "disabled");
			model.put("message", Message.newSuccess("Entity saved successfully."));
		}
		model.put("fsobject", newen);
		model.put("entity", TAB_HIGHLIGHTER);

		return "entity-edit";
	}

}