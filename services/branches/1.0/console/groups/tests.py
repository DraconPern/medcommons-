#!/usr/bin/env python

import re
import session
from test import ConsoleTest, Crawler

ID_RE = re.compile('id=([0-9]+)$')

class GroupTest(ConsoleTest):
    fixtures = ['test']

    def create_user(self, first_name, last_name, email, pw):
	r = self.client.get('/users/create')
	self.assertEquals(r.status_code, 200)
	self.assertValidXHTML(r.content)
	self.assertContains(r, 'MCID')

	mcid = r.context[0]['mcid']
	user = dict(mcid = mcid,
		    first_name = first_name, last_name = last_name)

	r = self.client.post('/users/create', user)
	self.assertRedirects(r, '/users/user?mcid=' + mcid)

	return user['mcid']

    def test_create_group(self):
	crawler = Crawler(self)

	self.login(username='root', password='medcommons')

	crawler.visit('/groups/')
	crawler.crawl()

	self.assertEquals(crawler.types['/groups/wiz_create'],
			  'text/html')

	mcid1 = self.create_user(first_name='Alphonse', last_name='Angstrom',
				 email='test1@medcommons.net', pw='welcome')

	mcid2 = self.create_user(first_name='Bertrand', last_name='Bulstrod',
				 email='test2@medcommons.net', pw='welcome')

	r = self.client.get('/groups/wiz_create')
	self.assertEquals(r.status_code, 200)
	self.assertValidXHTML(r.content)
	self.assertContains(r, mcid1)
	self.assertContains(r, mcid2)
	self.assertContains(r, 'Alphonse')
	self.assertContains(r, 'Bertrand')	

	r = self.client.post('/groups/wiz_create',
			     dict(owner_mcid = mcid1,
				  id = ''))
	id = ID_RE.findall(r['Location'])[0]
	self.assertRedirects(r, '/groups/wiz_name?id=%s' % id)

	r = self.get('/groups/wiz_name?id=%s' % id)
	self.assertEquals(r.status_code, 200)
	self.assertValidXHTML(r.content)

	user_url = '/groups/wiz_users?id=%s' % id
	r = self.client.post('/groups/wiz_name',
			     dict(id = id, name = "Alphonse's Group"))
	self.assertRedirects(r, user_url)

	r = self.get(user_url)
	self.assertEquals(r.status_code, 200)
	self.assertValidXHTML(r.content)

	# Add user
	r = self.client.post('/groups/wiz_users',
			     {'id': id, 'mcid': mcid2, 'add.x': 10, 'add.y': 6})
	self.assertRedirects(r, user_url)

	r = self.get(user_url)
	self.assertEquals(r.status_code, 200)
	self.assertValidXHTML(r.content)
	self.assertContains(r, 'Bertrand')

	r = self.client.post('/groups/wiz_users',
			     {'id': id, 'mcid': mcid2, 'remove.x': 10, 'remove.y': 6})
	self.assertRedirects(r, user_url)

	r = self.client.post('/groups/wiz_users',
			     {'id': id, 'finish': 'finish'})
	self.assertRedirects(r, '/groups/')

	crawler = Crawler(self)
	crawler.visit('/')
	crawler.crawl()
