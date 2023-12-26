<script lang="ts">
	import '../app.postcss';
	import { page } from '$app/stores';
	import { DarkMode, Navbar, NavBrand, NavHamburger, NavLi, NavUl } from 'flowbite-svelte';

	$: activePage = $page.url.pathname;

	let darkmodebtn =
		'text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none focus:ring-4 focus:ring-gray-200 dark:focus:ring-gray-700 rounded-lg text-lg p-2.5 fixed right-2 top-12  md:top-3 md:right-2 z-50';
	let divClass = 'w-full md:block md:w-auto pr-8';
	let ulClass =
		'flex flex-col p-4 mt-4 md:flex-row md:space-x-8 md:mt-0 md:text-lg md:font-semibold';

	const pages = [
		{ path: '/', title: 'Validate' },
		{ path: '/history', title: 'History' }
	];
</script>

<header>
	<Navbar let:hidden let:toggle>
		<NavBrand href="/" class="pl-8">
			<svg
				class="w-6 h-6 text-gray-800 dark:text-white"
				aria-hidden="true"
				xmlns="http://www.w3.org/2000/svg"
				fill="none"
				viewBox="0 0 20 20"
			>
				<path
					stroke="currentColor"
					stroke-linejoin="round"
					stroke-width="2"
					d="M10 6v4l3.276 3.276M19 10a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"
				/>
			</svg>
			<span class="self-center whitespace-nowrap text-xl font-semibold dark:text-white pl-4">
				Time Stamp Authority
			</span>
		</NavBrand>
		<NavHamburger on:click={toggle} />
		<NavUl {hidden} {divClass} {ulClass}>
			{#each pages as page}
				<NavLi href={page.path} active={activePage === page.path}>{page.title}</NavLi>
			{/each}
			<NavLi href="https://github.com/dnl50/tsa">
				<span class="inline-flex">
					<!-- only shown in burger extended burger menu -->
					<span {hidden} class="pr-1.5">GitHub</span>
					<!-- Flowbite GitHub Icon -->
					<svg
						class="w-6 h-6 text-gray-800 dark:text-white"
						aria-hidden="true"
						xmlns="http://www.w3.org/2000/svg"
						fill="currentColor"
						viewBox="0 0 20 20"
					>
						<path
							fill-rule="evenodd"
							d="M10 .333A9.911 9.911 0 0 0 6.866 19.65c.5.092.678-.215.678-.477 0-.237-.01-1.017-.014-1.845-2.757.6-3.338-1.169-3.338-1.169a2.627 2.627 0 0 0-1.1-1.451c-.9-.615.07-.6.07-.6a2.084 2.084 0 0 1 1.518 1.021 2.11 2.11 0 0 0 2.884.823c.044-.503.268-.973.63-1.325-2.2-.25-4.516-1.1-4.516-4.9A3.832 3.832 0 0 1 4.7 7.068a3.56 3.56 0 0 1 .095-2.623s.832-.266 2.726 1.016a9.409 9.409 0 0 1 4.962 0c1.89-1.282 2.717-1.016 2.717-1.016.366.83.402 1.768.1 2.623a3.827 3.827 0 0 1 1.02 2.659c0 3.807-2.319 4.644-4.525 4.889a2.366 2.366 0 0 1 .673 1.834c0 1.326-.012 2.394-.012 2.72 0 .263.18.572.681.475A9.911 9.911 0 0 0 10 .333Z"
							clip-rule="evenodd"
						/>
					</svg>
				</span>
			</NavLi>
		</NavUl>
	</Navbar>
	<DarkMode btnClass={darkmodebtn} />
</header>

<div class="flex lg:px-12 px-4 mx-auto w-full">
	<main class="w-full mx-auto">
		<slot />
	</main>
</div>
