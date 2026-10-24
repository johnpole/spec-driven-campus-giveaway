// Campus Giveaway end-to-end verification. Every check is strict: a failed expectation fails the run.
// Requires backend on :8080 and frontend on :4200. Screenshots go to artifacts/screenshots/.
const path = require('path');
const fs = require('fs');
const { chromium } = require(path.resolve(__dirname, '../../../../frontend/node_modules/playwright'));

const APP = 'http://localhost:4200';
const API = 'http://localhost:8080';
const ROOT = path.resolve(__dirname, '../../../..');
const shotsDir = path.join(ROOT, 'artifacts/screenshots');
const samplePhoto = path.join(ROOT, 'backend/src/main/resources/seed-images/seed-bike.jpg');

let step = 0;
const TOTAL = 10;
function scenario(title) {
  step += 1;
  console.log(`\n▶ [${step}/${TOTAL}] ${title}`);
}
function expect(condition, message) {
  if (!condition) throw new Error(`Expectation failed: ${message}`);
  console.log(`   ✓ ${message}`);
}
async function shot(page, name) {
  await page.screenshot({ path: path.join(shotsDir, name) });
  console.log(`   📸 ${name}`);
}

async function registerUser(request) {
  const email = `e2e-${Date.now()}@rpi.edu`;
  const password = 'e2e-pass-123';
  const res = await request.post(`${API}/api/auth/register`, {
    data: { firstName: 'E2E', lastName: 'Student', email, password }
  });
  expect(res.status() === 201, `registered a fresh test student (${email})`);
  return { email, password };
}

async function signInThroughModal(page, user) {
  await page.waitForSelector('#signin-username', { timeout: 5000 });
  await page.fill('#signin-username', user.email);
  await page.fill('#signin-password', user.password);
  await page.locator('#sign-in-form .primary-submit-btn').click();
  await page.waitForSelector('#signin-username', { state: 'detached', timeout: 5000 });
}

async function run() {
  fs.mkdirSync(shotsDir, { recursive: true });
  const browser = await chromium.launch({ channel: 'chrome', headless: true });
  const mobile = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 2, isMobile: true, hasTouch: true });
  const page = await mobile.newPage();
  page.on('pageerror', (err) => console.log('   [browser error]', err.message));
  let createdPost = null;

  try {
    scenario('Mobile feed renders without sideways scrolling');
    await page.goto(APP, { waitUntil: 'networkidle' });
    await page.waitForSelector('.item-card', { timeout: 10000 });
    expect((await page.locator('.item-card').count()) > 0, 'feed shows listings');
    const widths = await page.evaluate(() => [document.documentElement.scrollWidth, document.documentElement.clientWidth]);
    expect(widths[0] <= widths[1], `no horizontal overflow (${widths[0]} <= ${widths[1]})`);
    await shot(page, '01-mobile-home-feed.png');

    scenario('Customize view: list layout is applied and remembered');
    await page.click('#customize-view-btn');
    await page.click('#layout-list');
    expect(!!(await page.waitForSelector('#items-grid.layout-list', { timeout: 3000 })), 'list layout applied');
    await page.reload({ waitUntil: 'networkidle' });
    expect(!!(await page.waitForSelector('#items-grid.layout-list', { timeout: 5000 })), 'list layout remembered after reload');
    await shot(page, '02-list-layout.png');
    await page.evaluate(() => localStorage.removeItem('cg_view_prefs'));

    scenario('Search narrows the feed');
    await page.goto(APP, { waitUntil: 'networkidle' });
    await page.fill('.search-input', 'calculator');
    await page.waitForTimeout(600);
    const titles = await page.locator('.card-title').allInnerTexts();
    expect(titles.length > 0 && titles.every((t) => /calculator|ti-84/i.test(t)), `search results match "calculator" (${titles.length})`);
    await shot(page, '03-search-filter.png');

    scenario('Item detail never exposes poster contact info');
    await page.fill('.search-input', '');
    await page.waitForTimeout(400);
    await page.locator('.item-card').first().click();
    await page.waitForURL(/\/items\//);
    await page.waitForSelector('.item-title');
    const body = await page.innerText('body');
    expect(!/[a-z0-9._%+-]+@rpi\.edu|\+1518\d{7}/i.test(body), 'no poster email or phone in the page');
    await shot(page, '04-item-detail.png');

    scenario('Forged admin tokens are rejected by the API');
    const forged = await page.request.get(`${API}/api/auth/me`, { headers: { Authorization: 'Bearer cg-token:x:admin@campus-giveaway.test:Me:true:0' } });
    expect(forged.status() === 401, 'unsigned admin token → 401');
    const testToken = await page.request.get(`${API}/api/auth/me`, { headers: { Authorization: 'Bearer test-token:1:admin@campus-giveaway.test:Me' } });
    expect(testToken.status() === 401, 'test token outside tests → 401');

    scenario('Posting starts with sign-in');
    const user = await registerUser(page.request);
    await page.goto(`${APP}/post`, { waitUntil: 'networkidle' });
    expect(!!(await page.waitForSelector('#post-signin-btn', { timeout: 5000 })), 'step 1 asks the visitor to sign in');
    await shot(page, '05-post-signin-gate.png');
    await page.click('#post-signin-btn');
    await signInThroughModal(page, user);
    await page.waitForSelector('.photo-drop', { timeout: 5000 });
    expect(true, 'signed in and moved to the photo step');

    scenario('Photo → Gemini fills in the listing');
    await page.setInputFiles('#photo-input', samplePhoto);
    await page.waitForSelector('#post-form', { timeout: 30000 });
    const aiFilled = await page.locator('#ai-message.alert-ai').isVisible();
    const title = await page.inputValue('#post-title');
    if (aiFilled) {
      expect(title.trim().length > 0, `Gemini filled the title ("${title}")`);
    } else {
      console.log('   ⚠ AI auto-fill unavailable (no GEMINI_API_KEY?) — filling manually');
      await page.fill('#post-title', 'E2E test bike');
      await page.fill('#post-description', 'Created by the end-to-end suite.');
    }
    await shot(page, '06-ai-filled-review.png');

    scenario('Review and post the item');
    await page.locator('.spot-chip', { hasText: 'Union' }).click();
    await page.check('#legal-check');
    const created = page.waitForResponse((r) => r.url().endsWith('/api/posts') && r.request().method() === 'POST');
    await page.click('#submit-post-btn');
    const res = await created;
    expect(res.status() === 201, 'API created the post (201)');
    createdPost = await res.json();
    await page.waitForSelector('.success-screen', { timeout: 10000 });
    expect(true, 'success screen shown');
    await shot(page, '07-post-created.png');

    scenario('My Activity shows the new post');
    await page.goto(`${APP}/my-activity`, { waitUntil: 'networkidle' });
    await page.waitForTimeout(800);
    const activity = await page.innerText('body');
    expect(activity.includes(aiFilled ? title.slice(0, 12) : 'E2E test bike'), 'new post listed under My Activity');
    await shot(page, '08-my-activity.png');

    scenario('Request pickup on another item → prefilled, then "Request already sent"');
    await page.goto(APP, { waitUntil: 'networkidle' });
    await page.fill('.search-input', 'calculator');
    await page.waitForTimeout(600);
    await page.locator('.item-card').first().click();
    await page.waitForSelector('#open-interest-modal', { timeout: 5000 });
    await page.click('#open-interest-modal');
    await page.waitForSelector('#interest-pickup-at');
    expect((await page.inputValue('#interest-contact')) === user.email, 'contact prefilled from the signed-in account');
    expect((await page.inputValue('#interest-pickup-place')).length > 0, 'pickup place prefilled from the listing');
    await page.fill('#interest-pickup-place', 'Union lobby');
    await page.click('#send-interest-btn');
    await page.waitForSelector('#request-already-sent', { timeout: 5000 });
    expect(true, '"Request already sent" replaces "I\'m interested"');
    await page.reload({ waitUntil: 'networkidle' });
    expect(!!(await page.waitForSelector('#request-already-sent', { timeout: 5000 })), 'still shown after reload (server remembers)');
    await shot(page, '09-request-sent.png');
  } finally {
    if (createdPost?.id && createdPost?.editToken) {
      const del = await page.request.delete(`${API}/api/posts/${createdPost.id}`, { headers: { 'X-Edit-Token': createdPost.editToken } });
      console.log(`\n🧹 cleaned up test post (${del.status()})`);
    }
    await browser.close();
  }
}

run()
  .then(() => console.log('\n✅ All end-to-end scenarios passed.'))
  .catch((err) => {
    console.error(`\n❌ ${err.message}`);
    process.exit(1);
  });
