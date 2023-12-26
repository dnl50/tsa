<script>
	import { Heading } from 'flowbite-svelte';
	import { page } from '$app/stores';
</script>

<div class="flex h-56">
	<div class="text-center m-auto">
		<Heading tag="h1" class="mb-4" customSize="text-5xl md:text-4xl lg:text-4xl font-extrabold"
			>{$page.status} - {$page.error.message} 😵‍💫</Heading
		>
	</div>
</div>
