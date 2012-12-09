package sy.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sy.dao.BaseDaoI;
import sy.model.Tauth;
import sy.model.Tbug;
import sy.model.Tmenu;
import sy.model.Tonline;
import sy.model.Trole;
import sy.model.Troletauth;
import sy.model.Tuser;
import sy.model.Tusertrole;
import sy.service.RepairServiceI;
import sy.util.Encrypt;

/**
 * 修复数据库Service
 * 
 * @author 张代浩
 * 
 */
@Service("repairService")
public class RepairServiceImpl extends BaseServiceImpl implements RepairServiceI {

	private BaseDaoI<Tbug> bugDao;
	private BaseDaoI<Tuser> userDao;
	private BaseDaoI<Tmenu> menuDao;
	private BaseDaoI<Tonline> onlineDao;
	private BaseDaoI<Tauth> authDao;
	private BaseDaoI<Trole> roleDao;
	private BaseDaoI<Tusertrole> userroleDao;
	private BaseDaoI<Troletauth> roleauthDao;

	public BaseDaoI<Tbug> getBugDao() {
		return bugDao;
	}

	@Autowired
	public void setBugDao(BaseDaoI<Tbug> bugDao) {
		this.bugDao = bugDao;
	}

	public BaseDaoI<Troletauth> getRoleauthDao() {
		return roleauthDao;
	}

	@Autowired
	public void setRoleauthDao(BaseDaoI<Troletauth> roleauthDao) {
		this.roleauthDao = roleauthDao;
	}

	public BaseDaoI<Tusertrole> getUserroleDao() {
		return userroleDao;
	}

	@Autowired
	public void setUserroleDao(BaseDaoI<Tusertrole> userroleDao) {
		this.userroleDao = userroleDao;
	}

	public BaseDaoI<Trole> getRoleDao() {
		return roleDao;
	}

	@Autowired
	public void setRoleDao(BaseDaoI<Trole> roleDao) {
		this.roleDao = roleDao;
	}

	public BaseDaoI<Tauth> getAuthDao() {
		return authDao;
	}

	@Autowired
	public void setAuthDao(BaseDaoI<Tauth> authDao) {
		this.authDao = authDao;
	}

	public BaseDaoI<Tonline> getOnlineDao() {
		return onlineDao;
	}

	@Autowired
	public void setOnlineDao(BaseDaoI<Tonline> onlineDao) {
		this.onlineDao = onlineDao;
	}

	public BaseDaoI<Tmenu> getMenuDao() {
		return menuDao;
	}

	@Autowired
	public void setMenuDao(BaseDaoI<Tmenu> menuDao) {
		this.menuDao = menuDao;
	}

	public BaseDaoI<Tuser> getUserDao() {
		return userDao;
	}

	@Autowired
	public void setUserDao(BaseDaoI<Tuser> userDao) {
		this.userDao = userDao;
	}

	synchronized public void deleteAndRepair() {
		bugDao.executeHql("delete Tbug");
		onlineDao.executeHql("delete Tonline");
		menuDao.executeHql("update Tmenu t set t.tmenu = null");
		menuDao.executeHql("delete Tmenu");
		roleauthDao.executeHql("delete Troletauth");
		userroleDao.executeHql("delete Tusertrole");
		authDao.executeHql("update Tauth t set t.tauth = null");
		authDao.executeHql("delete Tauth");
		roleDao.executeHql("delete Trole");
		userDao.executeHql("delete Tuser");
		repair();
	}

	synchronized public void repair() {
		repairMenu();// 修复菜单
		repairAuth();// 修复权限
		repairRole();// 修复角色
		repairUser();// 修复用户
		repairRoleAuth();// 修复角色和权限的关系
		repairUserRole();// 修复用户和角色的关系
	}

	private void repairUserRole() {
		userroleDao.executeHql("delete Tusertrole t where t.tuser.cid in ( '0' )");

		Tusertrole userrole = new Tusertrole();
		userrole.setCid(UUID.randomUUID().toString());
		userrole.setTrole(roleDao.get(Trole.class, "0"));
		userrole.setTuser(userDao.get(Tuser.class, "0"));
		userroleDao.save(userrole);
	}

	private void repairRoleAuth() {
		roleauthDao.executeHql("delete Troletauth t where t.trole.cid = '0'");

		Trole role = roleDao.get(Trole.class, "0");

		List<Tauth> auths = authDao.find("from Tauth");
		if (auths != null && auths.size() > 0) {
			for (Tauth auth : auths) {
				Troletauth roleauth = new Troletauth();
				roleauth.setCid(UUID.randomUUID().toString());
				roleauth.setTrole(role);
				roleauth.setTauth(auth);
				roleauthDao.save(roleauth);
			}
		}
	}

	private void repairRole() {
		Trole admin = new Trole();
		admin.setCid("0");
		admin.setCname("超级管理员");
		admin.setCdesc("拥有系统所有权限");
		roleDao.saveOrUpdate(admin);

		Trole guest = new Trole();
		guest.setCid("1");
		guest.setCname("Guest");
		guest.setCdesc("");
		roleDao.saveOrUpdate(guest);
	}

	private void repairAuth() {
		authDao.executeHql("update Tauth a set a.tauth = null");

		Tauth easyssh = new Tauth();
		easyssh.setCid("0");
		easyssh.setTauth(null);
		easyssh.setCname("首页");
		easyssh.setCurl("");
		easyssh.setCseq(BigDecimal.valueOf(1));
		easyssh.setCdesc("EasySSH示例项目");
		authDao.saveOrUpdate(easyssh);

		Tauth sjkgl = new Tauth();
		sjkgl.setCid("sjkgl");
		sjkgl.setTauth(easyssh);
		sjkgl.setCname("数据库管理");
		sjkgl.setCurl("");
		sjkgl.setCseq(BigDecimal.valueOf(1));
		sjkgl.setCdesc("可查看数据库链接信息，SQL语句执行情况");
		authDao.saveOrUpdate(sjkgl);

		Tauth ljcjk = new Tauth();
		ljcjk.setCid("ljcjk");
		ljcjk.setTauth(sjkgl);
		ljcjk.setCname("连接池监控");
		ljcjk.setCurl("/datasourceAction!druid.action");
		ljcjk.setCseq(BigDecimal.valueOf(1));
		ljcjk.setCdesc("可查看数据库链接信息");
		authDao.saveOrUpdate(ljcjk);

		Tauth xtgl = new Tauth();
		xtgl.setCid("xtgl");
		xtgl.setTauth(easyssh);
		xtgl.setCname("系统管理");
		xtgl.setCurl("");
		xtgl.setCseq(BigDecimal.valueOf(2));
		xtgl.setCdesc("");
		authDao.saveOrUpdate(xtgl);

		Tauth yhgl = new Tauth();
		yhgl.setCid("yhgl");
		yhgl.setTauth(xtgl);
		yhgl.setCname("用户管理");
		yhgl.setCurl("");
		yhgl.setCseq(BigDecimal.valueOf(1));
		yhgl.setCdesc("");
		authDao.saveOrUpdate(yhgl);

		Tauth yhglym = new Tauth();
		yhglym.setCid("yhglym");
		yhglym.setTauth(yhgl);
		yhglym.setCname("用户管理页面");
		yhglym.setCurl("/userAction!user.action");
		yhglym.setCseq(BigDecimal.valueOf(1));
		yhglym.setCdesc("");
		authDao.saveOrUpdate(yhglym);

		Tauth yhcx = new Tauth();
		yhcx.setCid("yhcx");
		yhcx.setTauth(yhgl);
		yhcx.setCname("用户查询");
		yhcx.setCurl("/userAction!datagrid.action");
		yhcx.setCseq(BigDecimal.valueOf(2));
		yhcx.setCdesc("");
		authDao.saveOrUpdate(yhcx);

		Tauth yhadd = new Tauth();
		yhadd.setCid("yhadd");
		yhadd.setTauth(yhgl);
		yhadd.setCname("用户添加");
		yhadd.setCurl("/userAction!add.action");
		yhadd.setCseq(BigDecimal.valueOf(3));
		yhadd.setCdesc("");
		authDao.saveOrUpdate(yhadd);

		Tauth yhedit = new Tauth();
		yhedit.setCid("yhedit");
		yhedit.setTauth(yhgl);
		yhedit.setCname("用户修改");
		yhedit.setCurl("/userAction!edit.action");
		yhedit.setCseq(BigDecimal.valueOf(4));
		yhedit.setCdesc("");
		authDao.saveOrUpdate(yhedit);

		Tauth yhsc = new Tauth();
		yhsc.setCid("yhsc");
		yhsc.setTauth(yhgl);
		yhsc.setCname("用户删除");
		yhsc.setCurl("/userAction!delete.action");
		yhsc.setCseq(BigDecimal.valueOf(5));
		yhsc.setCdesc("");
		authDao.saveOrUpdate(yhsc);

		Tauth yhxgmm = new Tauth();
		yhxgmm.setCid("yhxgmm");
		yhxgmm.setTauth(yhgl);
		yhxgmm.setCname("修改密码");
		yhxgmm.setCurl("/userAction!modifyPwd.action");
		yhxgmm.setCseq(BigDecimal.valueOf(6));
		yhxgmm.setCdesc("批量修改用户密码");
		authDao.saveOrUpdate(yhxgmm);

		Tauth yhxgjs = new Tauth();
		yhxgjs.setCid("yhxgjs");
		yhxgjs.setTauth(yhgl);
		yhxgjs.setCname("修改角色");
		yhxgjs.setCurl("/userAction!modifyUserRole.action");
		yhxgjs.setCseq(BigDecimal.valueOf(7));
		yhxgjs.setCdesc("批量修改用户角色");
		authDao.saveOrUpdate(yhxgjs);

		Tauth jsgl = new Tauth();
		jsgl.setCid("jsgl");
		jsgl.setTauth(xtgl);
		jsgl.setCname("角色管理");
		jsgl.setCurl("");
		jsgl.setCseq(BigDecimal.valueOf(2));
		jsgl.setCdesc("");
		authDao.saveOrUpdate(jsgl);

		Tauth jsglym = new Tauth();
		jsglym.setCid("jsglym");
		jsglym.setTauth(jsgl);
		jsglym.setCname("角色管理页面");
		jsglym.setCurl("/roleAction!role.action");
		jsglym.setCseq(BigDecimal.valueOf(1));
		jsglym.setCdesc("");
		authDao.saveOrUpdate(jsglym);

		Tauth jscx = new Tauth();
		jscx.setCid("jscx");
		jscx.setTauth(jsgl);
		jscx.setCname("角色查询");
		jscx.setCurl("/roleAction!datagrid.action");
		jscx.setCseq(BigDecimal.valueOf(2));
		jscx.setCdesc("");
		authDao.saveOrUpdate(jscx);

		Tauth jsadd = new Tauth();
		jsadd.setCid("jsadd");
		jsadd.setTauth(jsgl);
		jsadd.setCname("角色添加");
		jsadd.setCurl("/roleAction!add.action");
		jsadd.setCseq(BigDecimal.valueOf(3));
		jsadd.setCdesc("");
		authDao.saveOrUpdate(jsadd);

		Tauth jsedit = new Tauth();
		jsedit.setCid("jsedit");
		jsedit.setTauth(jsgl);
		jsedit.setCname("角色编辑");
		jsedit.setCurl("/roleAction!edit.action");
		jsedit.setCseq(BigDecimal.valueOf(4));
		jsedit.setCdesc("");
		authDao.saveOrUpdate(jsedit);

		Tauth jsdelete = new Tauth();
		jsdelete.setCid("jsdelete");
		jsdelete.setTauth(jsgl);
		jsdelete.setCname("角色删除");
		jsdelete.setCurl("/roleAction!delete.action");
		jsdelete.setCseq(BigDecimal.valueOf(5));
		jsdelete.setCdesc("");
		authDao.saveOrUpdate(jsdelete);

		Tauth qxgl = new Tauth();
		qxgl.setCid("qxgl");
		qxgl.setTauth(xtgl);
		qxgl.setCname("权限管理");
		qxgl.setCurl("");
		qxgl.setCseq(BigDecimal.valueOf(3));
		qxgl.setCdesc("");
		authDao.saveOrUpdate(qxgl);

		Tauth qxglym = new Tauth();
		qxglym.setCid("qxglym");
		qxglym.setTauth(qxgl);
		qxglym.setCname("权限管理页面");
		qxglym.setCurl("/authAction!auth.action");
		qxglym.setCseq(BigDecimal.valueOf(1));
		qxglym.setCdesc("");
		authDao.saveOrUpdate(qxglym);

		Tauth qxcx = new Tauth();
		qxcx.setCid("qxcx");
		qxcx.setTauth(qxgl);
		qxcx.setCname("权限查询");
		qxcx.setCurl("/authAction!treegrid.action");
		qxcx.setCseq(BigDecimal.valueOf(2));
		qxcx.setCdesc("");
		authDao.saveOrUpdate(qxcx);

		Tauth qxadd = new Tauth();
		qxadd.setCid("qxadd");
		qxadd.setTauth(qxgl);
		qxadd.setCname("权限添加");
		qxadd.setCurl("/authAction!add.action");
		qxadd.setCseq(BigDecimal.valueOf(3));
		qxadd.setCdesc("");
		authDao.saveOrUpdate(qxadd);

		Tauth qxedit = new Tauth();
		qxedit.setCid("qxedit");
		qxedit.setTauth(qxgl);
		qxedit.setCname("权限编辑");
		qxedit.setCurl("/authAction!edit.action");
		qxedit.setCseq(BigDecimal.valueOf(4));
		qxedit.setCdesc("");
		authDao.saveOrUpdate(qxedit);

		Tauth qxdelete = new Tauth();
		qxdelete.setCid("qxdelete");
		qxdelete.setTauth(qxgl);
		qxdelete.setCname("权限删除");
		qxdelete.setCurl("/authAction!delete.action");
		qxdelete.setCseq(BigDecimal.valueOf(5));
		qxdelete.setCdesc("");
		authDao.saveOrUpdate(qxdelete);

		Tauth cdgl = new Tauth();
		cdgl.setCid("cdgl");
		cdgl.setTauth(xtgl);
		cdgl.setCname("菜单管理");
		cdgl.setCurl("");
		cdgl.setCseq(BigDecimal.valueOf(4));
		cdgl.setCdesc("");
		authDao.saveOrUpdate(cdgl);

		Tauth cdglym = new Tauth();
		cdglym.setCid("cdglym");
		cdglym.setTauth(cdgl);
		cdglym.setCname("菜单管理页面");
		cdglym.setCurl("/menuAction!menu.action");
		cdglym.setCseq(BigDecimal.valueOf(1));
		cdglym.setCdesc("");
		authDao.saveOrUpdate(cdglym);

		Tauth cdcx = new Tauth();
		cdcx.setCid("cdcx");
		cdcx.setTauth(cdgl);
		cdcx.setCname("菜单查询");
		cdcx.setCurl("/menuAction!treegrid.action");
		cdcx.setCseq(BigDecimal.valueOf(2));
		cdcx.setCdesc("");
		authDao.saveOrUpdate(cdcx);

		Tauth cdadd = new Tauth();
		cdadd.setCid("cdadd");
		cdadd.setTauth(cdgl);
		cdadd.setCname("菜单添加");
		cdadd.setCurl("/menuAction!add.action");
		cdadd.setCseq(BigDecimal.valueOf(3));
		cdadd.setCdesc("");
		authDao.saveOrUpdate(cdadd);

		Tauth cdedit = new Tauth();
		cdedit.setCid("cdedit");
		cdedit.setTauth(cdgl);
		cdedit.setCname("菜单编辑");
		cdedit.setCurl("/menuAction!edit.action");
		cdedit.setCseq(BigDecimal.valueOf(4));
		cdedit.setCdesc("");
		authDao.saveOrUpdate(cdedit);

		Tauth cddelete = new Tauth();
		cddelete.setCid("cddelete");
		cddelete.setTauth(cdgl);
		cddelete.setCname("菜单删除");
		cddelete.setCurl("/menuAction!delete.action");
		cddelete.setCseq(BigDecimal.valueOf(5));
		cddelete.setCdesc("");
		authDao.saveOrUpdate(cddelete);

		Tauth buggl = new Tauth();
		buggl.setCid("buggl");
		buggl.setTauth(xtgl);
		buggl.setCname("BUG管理");
		buggl.setCurl("");
		buggl.setCseq(BigDecimal.valueOf(5));
		buggl.setCdesc("");
		authDao.saveOrUpdate(buggl);

		Tauth bugglym = new Tauth();
		bugglym.setCid("bugglym");
		bugglym.setTauth(buggl);
		bugglym.setCname("BUG管理页面");
		bugglym.setCurl("/bugAction!bug.action");
		bugglym.setCseq(BigDecimal.valueOf(1));
		bugglym.setCdesc("");
		authDao.saveOrUpdate(bugglym);

		Tauth bugcx = new Tauth();
		bugcx.setCid("bugcx");
		bugcx.setTauth(buggl);
		bugcx.setCname("BUG查询");
		bugcx.setCurl("/bugAction!datagrid.action");
		bugcx.setCseq(BigDecimal.valueOf(2));
		bugcx.setCdesc("");
		authDao.saveOrUpdate(bugcx);

		Tauth bugadd = new Tauth();
		bugadd.setCid("bugadd");
		bugadd.setTauth(buggl);
		bugadd.setCname("BUG描述添加");
		bugadd.setCurl("/bugAction!add.action");
		bugadd.setCseq(BigDecimal.valueOf(3));
		bugadd.setCdesc("");
		authDao.saveOrUpdate(bugadd);

		Tauth bugedit = new Tauth();
		bugedit.setCid("bugedit");
		bugedit.setTauth(buggl);
		bugedit.setCname("BUG编辑");
		bugedit.setCurl("/bugAction!edit.action");
		bugedit.setCseq(BigDecimal.valueOf(4));
		bugedit.setCdesc("");
		authDao.saveOrUpdate(bugedit);

		Tauth bugdelete = new Tauth();
		bugdelete.setCid("bugdelete");
		bugdelete.setTauth(buggl);
		bugdelete.setCname("BUG删除");
		bugdelete.setCurl("/bugAction!delete.action");
		bugdelete.setCseq(BigDecimal.valueOf(5));
		bugdelete.setCdesc("");
		authDao.saveOrUpdate(bugdelete);

		Tauth bugupload = new Tauth();
		bugupload.setCid("bugupload");
		bugupload.setTauth(buggl);
		bugupload.setCname("BUG上传");
		bugupload.setCurl("/bugAction!upload.action");
		bugupload.setCseq(BigDecimal.valueOf(6));
		bugupload.setCdesc("");
		authDao.saveOrUpdate(bugupload);

	}

	private void repairMenu() {
		menuDao.executeHql("update Tmenu m set m.tmenu = null");

		Tmenu root = new Tmenu();
		root.setCid("0");
		root.setCname("首页");
		root.setCseq(BigDecimal.valueOf(1));
		root.setCurl("");
		root.setTmenu(null);
		menuDao.saveOrUpdate(root);

		Tmenu sjkgl = new Tmenu();
		sjkgl.setCid("sjkgl");
		sjkgl.setCname("数据库管理");
		sjkgl.setCseq(BigDecimal.valueOf(2));
		sjkgl.setCurl("");
		sjkgl.setTmenu(root);
		menuDao.saveOrUpdate(sjkgl);

		Tmenu druidIndex = new Tmenu();
		druidIndex.setCid("druidIndex");
		druidIndex.setCname("druid监控");
		druidIndex.setCseq(BigDecimal.valueOf(3));
		druidIndex.setCurl("datasourceAction!druid.action");
		druidIndex.setTmenu(sjkgl);
		menuDao.saveOrUpdate(druidIndex);

		Tmenu xtgl = new Tmenu();
		xtgl.setCid("xtgl");
		xtgl.setCname("系统管理");
		xtgl.setCseq(BigDecimal.valueOf(3));
		xtgl.setCurl("");
		xtgl.setTmenu(root);
		menuDao.saveOrUpdate(xtgl);

		Tmenu yhgl = new Tmenu();
		yhgl.setCid("yhgl");
		yhgl.setCname("用户管理");
		yhgl.setCseq(BigDecimal.valueOf(1));
		yhgl.setCurl("userAction!user.action");
		yhgl.setTmenu(xtgl);
		menuDao.saveOrUpdate(yhgl);

		Tmenu jsgl = new Tmenu();
		jsgl.setCid("jsgl");
		jsgl.setCname("角色管理");
		jsgl.setCseq(BigDecimal.valueOf(2));
		jsgl.setCurl("roleAction!role.action");
		jsgl.setTmenu(xtgl);
		menuDao.saveOrUpdate(jsgl);

		Tmenu qxgl = new Tmenu();
		qxgl.setCid("qxgl");
		qxgl.setCname("权限管理");
		qxgl.setCseq(BigDecimal.valueOf(3));
		qxgl.setCurl("authAction!auth.action");
		qxgl.setTmenu(xtgl);
		menuDao.saveOrUpdate(qxgl);

		Tmenu cdgl = new Tmenu();
		cdgl.setCid("cdgl");
		cdgl.setCname("菜单管理");
		cdgl.setCseq(BigDecimal.valueOf(4));
		cdgl.setCurl("menuAction!menu.action");
		cdgl.setTmenu(xtgl);
		menuDao.saveOrUpdate(cdgl);

		Tmenu buggl = new Tmenu();
		buggl.setCid("buggl");
		buggl.setCname("BUG管理");
		buggl.setCseq(BigDecimal.valueOf(5));
		buggl.setCurl("bugAction!bug.action");
		buggl.setTmenu(xtgl);
		menuDao.saveOrUpdate(buggl);

		Tmenu rzgl = new Tmenu();
		rzgl.setCid("rzgl");
		rzgl.setCname("日志管理");
		rzgl.setCseq(BigDecimal.valueOf(4));
		rzgl.setCurl("");
		rzgl.setTmenu(root);
		menuDao.saveOrUpdate(rzgl);

		Tmenu yhdlrz = new Tmenu();
		yhdlrz.setCid("yhdlrz");
		yhdlrz.setCname("用户登录日志");
		yhdlrz.setCseq(BigDecimal.valueOf(1));
		yhdlrz.setCurl("");
		yhdlrz.setTmenu(rzgl);
		menuDao.saveOrUpdate(yhdlrz);

		Tmenu yhzxrz = new Tmenu();
		yhzxrz.setCid("yhzxrz");
		yhzxrz.setCname("用户注销日志");
		yhzxrz.setCseq(BigDecimal.valueOf(2));
		yhzxrz.setCurl("");
		yhzxrz.setTmenu(rzgl);
		menuDao.saveOrUpdate(yhzxrz);

		Tmenu yhglrz = new Tmenu();
		yhglrz.setCid("yhglrz");
		yhglrz.setCname("用户管理日志");
		yhglrz.setCseq(BigDecimal.valueOf(3));
		yhglrz.setCurl("");
		yhglrz.setTmenu(rzgl);
		menuDao.saveOrUpdate(yhglrz);

		Tmenu jsglrz = new Tmenu();
		jsglrz.setCid("jsglrz");
		jsglrz.setCname("角色管理日志");
		jsglrz.setCseq(BigDecimal.valueOf(4));
		jsglrz.setCurl("");
		jsglrz.setTmenu(rzgl);
		menuDao.saveOrUpdate(jsglrz);

		Tmenu qxglrz = new Tmenu();
		qxglrz.setCid("qxglrz");
		qxglrz.setCname("权限管理日志");
		qxglrz.setCseq(BigDecimal.valueOf(5));
		qxglrz.setCurl("");
		qxglrz.setTmenu(rzgl);
		menuDao.saveOrUpdate(qxglrz);

		Tmenu cdglrz = new Tmenu();
		cdglrz.setCid("cdglrz");
		cdglrz.setCname("菜单管理日志");
		cdglrz.setCseq(BigDecimal.valueOf(6));
		cdglrz.setCurl("");
		cdglrz.setTmenu(rzgl);
		menuDao.saveOrUpdate(cdglrz);

	}

	private void repairUser() {
		List<Tuser> t = userDao.find("from Tuser t where t.cname = ? and t.cid != ?", "admin", "0");// cid!='0'并且cname='admin'这是错误的数据，需要修复
		if (t != null && t.size() > 0) {
			for (Tuser u : t) {
				u.setCname(u.getCname() + UUID.randomUUID().toString());
			}
		}

		Tuser admin = new Tuser();
		admin.setCid("0");
		admin.setCname("admin");
		admin.setCpwd(Encrypt.e("admin"));
		userDao.saveOrUpdate(admin);
	}

}